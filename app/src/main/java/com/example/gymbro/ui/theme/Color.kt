package com.example.gymbro.ui.theme

import androidx.compose.ui.graphics.Color

// ── Neo-Brutalist Palette ──────────────────────────────────────────

// ── Light Mode ──
val BrutalistLightBackground  = Color(0xFFFFFDF5)   // warm off-white
val BrutalistLightSurface     = Color(0xFFFFFDF5)
val BrutalistLightCard        = Color(0xFFFFFFFF)    // pure white cards
val BrutalistLightBorder      = Color(0xFF1A1A1A)    // near-black borders
val BrutalistLightShadow      = Color(0xFF1A1A1A)    // hard shadow
val BrutalistLightText        = Color(0xFF1A1A1A)    // primary text
val BrutalistLightTextMuted   = Color(0xFF6B6B6B)    // secondary text
val BrutalistLightNavBar      = Color(0xFFFFFFFF)    // bottom bar bg

// ── Dark Mode (AMOLED) ──
val BrutalistDarkBackground   = Color(0xFF000000)    // true AMOLED black
val BrutalistDarkSurface      = Color(0xFF000000)
val BrutalistDarkCard         = Color(0xFF141414)    // very dark card
val BrutalistDarkBorder       = Color(0xFFE0E0E0)    // light borders on dark
val BrutalistDarkShadow       = Color(0xFF333333)    // subtle shadow on dark
val BrutalistDarkText         = Color(0xFFF5F5F5)    // off-white text
val BrutalistDarkTextMuted    = Color(0xFF9E9E9E)    // muted text
val BrutalistDarkNavBar       = Color(0xFF0A0A0A)    // bottom bar bg

// ── Shared Accents (same in both modes) ──
val BrutalistYellow  = Color(0xFFFFE135)   // ⚡ electric yellow — primary
val BrutalistPink    = Color(0xFFFF6B9D)   // 🌸 hot pink — secondary
val BrutalistBlue    = Color(0xFF4169E1)   // 🔵 royal blue — tertiary
val BrutalistLime    = Color(0xFFA8E610)   // 🟢 lime — success / went to gym
val BrutalistRed     = Color(0xFFFF3B30)   // 🔴 skipped
val BrutalistPurple  = Color(0xFF9B59B6)   // 🟣 excused
val BrutalistGray    = Color(0xFFBDBDBD)   // ⚪ rest day

// Mapped Status Names (backward-compat)
val StatusGreen  = BrutalistLime
val StatusRed    = BrutalistRed
val StatusPurple = BrutalistPurple
val StatusGrey   = BrutalistGray

// Legacy aliases so ViewModels compile untouched
val NeonGreen  = BrutalistLime
val NeonBlue   = BrutalistBlue
val NeonRed    = BrutalistRed
val NeonPurple = BrutalistPurple
val NeonOrange = BrutalistYellow
val White      = Color(0xFFFFFFFF)
val Black      = Color(0xFF000000)
val CardGray   = Color(0xFF1E1E1E)
val DarkGray   = Color(0xFF121212)
val LightGray  = Color(0xFF2C2C2C)
