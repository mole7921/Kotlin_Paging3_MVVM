package com.enzo.architecture.api

import com.enzo.architecture.model.UnsplashPhoto


data class UnsplashResponse(
    val results: List<UnsplashPhoto>
)