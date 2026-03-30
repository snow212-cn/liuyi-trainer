package com.liuyi.trainer.model

enum class VoiceGuideMode {
    Command,
    Counting,
    Breathing,
}

data class DeviceVoiceOption(
    val id: String,
    val label: String,
    val languageTag: String = "",
)
