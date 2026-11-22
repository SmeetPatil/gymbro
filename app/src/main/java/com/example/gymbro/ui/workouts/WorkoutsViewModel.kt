package com.example.gymbro.ui.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbro.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray

@HiltViewModel
class WorkoutsViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val splitDao: SplitDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val allWorkouts = workoutDao.getAllWorkouts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSplits = splitDao.getAllSplits().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeSplit = splitDao.getActiveSplit().stateIn(viewModelScope, SharingStarted.Lazily, null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeSplitDaysWithWorkouts = activeSplit.flatMapLatest { split ->
        if (split == null) flowOf(emptyList())
        else splitDao.getSplitDaysWithWorkouts(split.id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Search and Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<MuscleGroup?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    val filteredWorkouts = combine(allWorkouts, _searchQuery, _selectedCategory) { workouts, query, category ->
        workouts.filter { workout ->
            val matchesQuery = workout.name.contains(query, ignoreCase = true)
            val matchesCategory = category == null || workout.muscleGroup == category
            matchesQuery && matchesCategory
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // For creating a new split
    private val _newSplitName = MutableStateFlow("")
    val newSplitName = _newSplitName.asStateFlow()
    
    private val _newSplitDays = MutableStateFlow<Map<Int, List<Workout>>>(emptyMap())
    val newSplitDays = _newSplitDays.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: MuscleGroup?) {
        _selectedCategory.value = category
    }

    fun addWorkoutToSplit(splitId: Int, dayOfWeek: Int, workoutId: Int) {
        viewModelScope.launch {
            splitDao.insertSplitDay(SplitDay(splitId, dayOfWeek, workoutId))
        }
    }

    fun updateSplitDaySetsAndReps(splitId: Int, dayOfWeek: Int, workoutId: Int, sets: Int, repRange: String) {
        viewModelScope.launch {
            splitDao.updateSplitDaySetsAndReps(splitId, dayOfWeek, workoutId, sets, repRange)
        }
    }

    fun removeWorkoutFromSplitDay(splitId: Int, dayOfWeek: Int, workoutId: Int) {
        viewModelScope.launch {
            splitDao.deleteSplitDayWorkout(splitId, dayOfWeek, workoutId)
        }
    }

    fun getDayTitle(workouts: List<Workout>): String {
        if (workouts.isEmpty()) return ""
        val muscles = workouts.map { it.muscleGroup }.distinct()
        val arms = listOf(MuscleGroup.BICEPS, MuscleGroup.TRICEPS)
        // Sort so arms come last
        val sortedMuscles = muscles.sortedBy { if (it in arms) 1 else 0 }
        return sortedMuscles.joinToString(" - ") { it.name.lowercase().capitalize() }
    }

    fun setActiveSplit(splitId: Int) {
        viewModelScope.launch {
            splitDao.setActiveSplit(splitId)
        }
    }

    fun createSplit(name: String) {
        viewModelScope.launch {
            val splitsList = allSplits.value
            val maxOrder = splitsList.maxOfOrNull { it.orderIndex } ?: -1
            val splitId = splitDao.insertSplit(Split(name = name, isActive = false, orderIndex = maxOrder + 1)).toInt()
            _newSplitDays.value.forEach { (day, workouts) ->
                workouts.forEach { workout ->
                    splitDao.insertSplitDay(SplitDay(splitId, day, workout.id))
                }
            }
            _newSplitName.value = ""
            _newSplitDays.value = emptyMap()
        }
    }

    fun updateSplitOrder(splits: List<Split>) {
        viewModelScope.launch {
            splitDao.updateSplits(splits)
        }
    }

    fun deleteSplit(id: Int) {
        viewModelScope.launch {
            splitDao.deleteSplitWithDays(id)
        }
    }
    
    fun addWorkoutToNewSplit(day: Int, workout: Workout) {
        val current = _newSplitDays.value.toMutableMap()
        val list = current[day]?.toMutableList() ?: mutableListOf()
        list.add(workout)
        current[day] = list
        _newSplitDays.value = current
    }

    // Seed data if empty and deduplicate
    fun seedWorkouts() {
        viewModelScope.launch(Dispatchers.IO) {
            // Run once, off the main thread: parsing the ~1MB exercise JSON on every
            // screen entry caused main-thread jank. Gate on a prefs flag.
            val prefs = context.getSharedPreferences("gymbro_prefs", Context.MODE_PRIVATE)
            if (prefs.getBoolean("workouts_seeded_v1", false)) return@launch

            // 1. Deduplicate any existing workouts (including plurals)
            val currentWorkouts = workoutDao.getAllWorkoutsSync()
            val groupedByName = currentWorkouts.groupBy { 
                var normalized = it.name.trim().lowercase()
                // Simple plural normalization (strip trailing 's' unless it ends in 'ss' like press)
                if (normalized.endsWith("s") && !normalized.endsWith("ss") && !normalized.endsWith("iceps")) {
                    normalized = normalized.dropLast(1)
                }
                normalized
            }
            
            for ((_, list) in groupedByName) {
                if (list.size > 1) {
                    // Prefer the singular name or the shortest name as the one to keep
                    val keepWorkout = list.minByOrNull { it.name.length }!!
                    val keepId = keepWorkout.id
                    val duplicateIds = list.map { it.id } - keepId
                    for (dupId in duplicateIds) {
                        splitDao.remapWorkoutId(oldId = dupId, newId = keepId)
                        workoutDao.deleteWorkout(dupId)
                    }
                }
            }

            // 2. Fetch the newly deduplicated list to check existing names
            val deduplicatedWorkouts = workoutDao.getAllWorkoutsSync()
            val existingNames = deduplicatedWorkouts.map { 
                var n = it.name.trim().lowercase()
                if (n.endsWith("s") && !n.endsWith("ss") && !n.endsWith("iceps")) n.dropLast(1) else n
            }.toSet()

            // 3. Prepare our master list of exercises (comprehensive)
            val masterList = mutableListOf<Workout>()

            try {
                val inputStream = context.resources.openRawResource(com.example.gymbro.R.raw.free_exercises)
                val jsonText = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonText)

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val name = obj.optString("name").takeIf { it.isNotBlank() } ?: continue
                    
                    val primaryMusclesArray = obj.optJSONArray("primaryMuscles")
                    val primaryMuscles = mutableListOf<String>()
                    if (primaryMusclesArray != null) {
                        for (j in 0 until primaryMusclesArray.length()) {
                            primaryMuscles.add(primaryMusclesArray.getString(j))
                        }
                    }
                    
                    val categoryStr = obj.optString("category", "other")
                    
                    val instructionsArray = obj.optJSONArray("instructions")
                    val steps = mutableListOf<String>()
                    if (instructionsArray != null) {
                        for (j in 0 until instructionsArray.length()) {
                            steps.add(instructionsArray.getString(j))
                        }
                    }
                    val description = if (steps.isNotEmpty()) steps.joinToString("\n\n") else "No description available."
                    
                    // Map category and primary muscles to MuscleGroup
                    val searchStr = (categoryStr + " " + primaryMuscles.joinToString(" ")).lowercase()
                    val muscleGroup = when {
                        searchStr.contains("chest") || searchStr.contains("pecs") -> MuscleGroup.CHEST
                        searchStr.contains("back") || searchStr.contains("lat") || searchStr.contains("trap") -> MuscleGroup.BACK
                        searchStr.contains("leg") || searchStr.contains("calv") || searchStr.contains("quad") || searchStr.contains("hamstring") || searchStr.contains("glute") -> MuscleGroup.LEGS
                        searchStr.contains("shoulder") || searchStr.contains("delt") -> MuscleGroup.SHOULDERS
                        searchStr.contains("bicep") || searchStr.contains("forearm") -> MuscleGroup.BICEPS
                        searchStr.contains("tricep") -> MuscleGroup.TRICEPS
                        searchStr.contains("abs") || searchStr.contains("core") || searchStr.contains("oblique") || searchStr.contains("abdominals") -> MuscleGroup.ABS
                        else -> MuscleGroup.CARDIO
                    }
                    
                    // Map to a generic silhouette icon based on category for simplicity
                    val iconName = when (muscleGroup) {
                        MuscleGroup.CHEST -> "ic_bench"
                        MuscleGroup.BACK -> "ic_deadlift"
                        MuscleGroup.LEGS -> "ic_squat"
                        MuscleGroup.SHOULDERS -> "ic_ohp"
                        MuscleGroup.BICEPS -> "ic_db_curl"
                        MuscleGroup.TRICEPS -> "ic_pushdown"
                        MuscleGroup.ABS -> "ic_crunch"
                        MuscleGroup.CARDIO -> "ic_treadmill"
                    }

                    masterList.add(
                        Workout(
                            name = name,
                            muscleGroup = muscleGroup,
                            silhouetteIconName = iconName,
                            description = description
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 4. Insert only new workouts that don't already exist
            val newWorkouts = masterList.filter { 
                var n = it.name.trim().lowercase()
                if (n.endsWith("s") && !n.endsWith("ss") && !n.endsWith("iceps")) n = n.dropLast(1)
                n !in existingNames 
            }
            if (newWorkouts.isNotEmpty()) {
                workoutDao.insertWorkouts(newWorkouts)
            }

            // Mark seeded only once the table is populated, so a failed/empty parse
            // retries on the next launch rather than being skipped forever.
            if (workoutDao.getAllWorkoutsSync().isNotEmpty()) {
                prefs.edit().putBoolean("workouts_seeded_v1", true).apply()
            }
        }
    }
}
