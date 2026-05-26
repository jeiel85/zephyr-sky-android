package com.jeiel.zephyr_sky

import com.jeiel.zephyr_sky.data.repository.normalizeCitySearchQuery
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherRepositoryTest {
    @Test
    fun koreanCityNamesAreNormalizedForOpenMeteoSearch() {
        assertEquals("Seoul", normalizeCitySearchQuery("서울"))
        assertEquals("Seoul", normalizeCitySearchQuery("서울특별시"))
        assertEquals("Busan", normalizeCitySearchQuery("부산"))
        assertEquals("Jeju", normalizeCitySearchQuery("제주"))
    }

    @Test
    fun unknownCityNameIsPreserved() {
        assertEquals("Tokyo", normalizeCitySearchQuery(" Tokyo "))
        assertEquals("춘천", normalizeCitySearchQuery("춘천"))
    }
}
