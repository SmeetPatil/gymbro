package com.example.gymbro.ui.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymbro.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class WorkoutsViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val splitDao: SplitDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val allWorkouts = workoutDao.getAllWorkouts().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSplits = splitDao.getAllSplits().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeSplit = splitDao.getActiveSplit().stateIn(viewModelScope, SharingStarted.Lazily, null)

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

    fun setActiveSplit(splitId: Int) {
        viewModelScope.launch {
            splitDao.setActiveSplit(splitId)
        }
    }

    fun createSplit(name: String) {
        viewModelScope.launch {
            val splitId = splitDao.insertSplit(Split(name = name, isActive = false)).toInt()
            _newSplitDays.value.forEach { (day, workouts) ->
                workouts.forEach { workout ->
                    splitDao.insertSplitDay(SplitDay(splitId, day, workout.id))
                }
            }
            // Reset creation state
            _newSplitName.value = ""
            _newSplitDays.value = emptyMap()
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
        viewModelScope.launch {
            // 1. Deduplicate any existing workouts
            val currentWorkouts = workoutDao.getAllWorkoutsSync()
            val groupedByName = currentWorkouts.groupBy { it.name.trim().lowercase() }
            
            for ((_, list) in groupedByName) {
                if (list.size > 1) {
                    val keepId = list.minByOrNull { it.id }!!.id
                    val duplicateIds = list.map { it.id } - keepId
                    for (dupId in duplicateIds) {
                        splitDao.remapWorkoutId(oldId = dupId, newId = keepId)
                        workoutDao.deleteWorkout(dupId)
                    }
                }
            }

            // 2. Fetch the newly deduplicated list to check existing names
            val deduplicatedWorkouts = workoutDao.getAllWorkoutsSync()
            val existingNames = deduplicatedWorkouts.map { it.name.trim().lowercase() }.toSet()

            // 3. Prepare our master list of exercises (comprehensive)
            val masterList = mutableListOf<Workout>()

            try {
                val inputStream = context.resources.openRawResource(com.example.gymbro.R.raw.musclewiki_exercises)
                val reader = inputStream.bufferedReader()
                reader.readLine() // skip header
                reader.forEachLine { line ->
                    val tokens = line.split(",")
                    if (tokens.size >= 3) {
                        val categoryStr = tokens[0].trim()
                        val equipment = tokens[1].trim()
                        val name = tokens[2].trim()

                        // Map category to MuscleGroup
                        val muscleGroup = when (categoryStr.lowercase()) {
                            "chest" -> MuscleGroup.CHEST
                            "back", "lats", "lower back", "traps" -> MuscleGroup.BACK
                            "legs", "calves", "quads", "hamstrings", "glutes" -> MuscleGroup.LEGS
                            "shoulders", "delts" -> MuscleGroup.SHOULDERS
                            "biceps", "forearms" -> MuscleGroup.BICEPS
                            "triceps" -> MuscleGroup.TRICEPS
                            "abs", "core", "obliques" -> MuscleGroup.ABS
                            "cardio" -> MuscleGroup.CARDIO
                            else -> MuscleGroup.CARDIO // Fallback
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
                        
                        val description = "Primary muscle: $categoryStr. Equipment needed: $equipment. Focus on form and controlled movement."

                        masterList.add(
                            Workout(
                                name = name,
                                muscleGroup = muscleGroup,
                                silhouetteIconName = iconName,
                                description = description
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 4. Insert only new workouts that don't already exist
            val newWorkouts = masterList.filter { it.name.trim().lowercase() !in existingNames }
            if (newWorkouts.isNotEmpty()) {
                workoutDao.insertWorkouts(newWorkouts)
            }
        }
    }
}
