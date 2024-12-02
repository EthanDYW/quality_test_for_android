package com.example.qualitytestforandroid.data

import java.io.Serializable

data class DefectTypeData(
    val name: String,
    val okPhotos: List<String>,
    val ngPhotos: List<String>
) : Serializable
