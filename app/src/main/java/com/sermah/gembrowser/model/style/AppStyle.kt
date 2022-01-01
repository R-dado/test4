package com.sermah.gembrowser.model.style

data class AppStyle (
    var primaryBackground: DayNightColor = DayNightColor(),
    var secondaryBackground: DayNightColor = DayNightColor(),
    var contentBackground: DayNightColor = DayNightColor(),

    var primaryText: DayNightColor = DayNightColor.TextDefault,
    var secondaryText: DayNightColor = DayNightColor.TextDefault,
)