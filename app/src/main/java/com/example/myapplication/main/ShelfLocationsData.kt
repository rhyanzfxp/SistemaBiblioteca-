package com.example.myapplication.main

import android.graphics.RectF


val areasTerreo: Map<String, Area> = mapOf(
    "0A" to Area("0A", 8f, 8f, 442f, 30f),
    "0B" to Area("0B", 8f, 45f, 442f, 30f),
    "0C" to Area("0C", 8f, 82f, 442f, 30f),
    "0D" to Area("0D", 8f, 119f, 442f, 30f),
    "0E" to Area("0E", 8f, 156f, 442f, 30f),
    "0F" to Area("0F", 8f, 193f, 442f, 30f),
    "0G" to Area("0G", 8f, 230f, 442f, 30f),
    "0H" to Area("0H", 8f, 267f, 442f, 30f),
    "0I" to Area("0I", 8f, 304f, 442f, 30f),
    "0J" to Area("0J", 8f, 341f, 442f, 30f),
    "0K" to Area("0K", 8f, 378f, 442f, 30f),
    "0L" to Area("0L", 8f, 415f, 442f, 30f),
    "0M" to Area("0M", 8f, 452f, 442f, 30f),
    "0N" to Area("0N", 8f, 489f, 442f, 30f),
    "0O" to Area("0O", 8f, 526f, 442f, 30f),
    "0P" to Area("0P", 8f, 563f, 442f, 30f),
    "0Q" to Area("0Q", 8f, 600f, 442f, 30f),
    "0R" to Area("0R", 8f, 637f, 245f, 30f),
    "0S" to Area("0S", 8f, 674f, 245f, 30f),
    "0T" to Area("0T", 8f, 711f, 245f, 30f),
    "0U" to Area("0U", 490f, 8f, 126f, 30f),
    "0V" to Area("0V", 490f, 45f, 126f, 30f),
    "0W" to Area("0W", 490f, 82f, 126f, 30f),
    "0X" to Area("0X", 490f, 119f, 126f, 30f),
    "0Y" to Area("0Y", 490f, 156f, 126f, 30f),
    "0Z" to Area("0Z", 490f, 193f, 126f, 30f),
    "0AA" to Area("0AA", 490f, 230f, 126f, 30f),
    "0AB" to Area("0AB", 490f, 267f, 126f, 30f),
    "0AC" to Area("0AC", 490f, 304f, 126f, 30f),
    "0AD" to Area("0AD", 490f, 341f, 126f, 30f),
    "0AE" to Area("0AE", 490f, 378f, 126f, 30f),
    "0AF" to Area("0AF", 490f, 415f, 126f, 30f),
    "0AG" to Area("0AG", 490f, 452f, 126f, 30f),
    "0AH" to Area("0AH", 490f, 489f, 126f, 30f),
    "0AI" to Area("0AI", 490f, 526f, 106f, 30f),
    "0AJ" to Area("0AJ", 490f, 563f, 106f, 30f),
    "0AK" to Area("0AK", 490f, 600f, 106f, 30f),
    "0AL" to Area("0AL", 490f, 637f, 106f, 30f),
    "0AM" to Area("0AM", 490f, 674f, 106f, 30f)
)

val areasSuperior: Map<String, Area> = mapOf(
    "1A" to Area("1A", 185f, 18f, 100f, 20f),
    "1B" to Area("1B", 185f, 38f, 100f, 22f),
    "1C" to Area("1C", 185f, 68f, 100f, 22f),
    "1D" to Area("1D", 185f, 98f, 100f, 22f),
    "1E" to Area("1E", 185f, 138f, 100f, 22f),
    "1F" to Area("1F", 185f, 168f, 100f, 22f),
    "1G" to Area("1G", 185f, 198f, 100f, 22f),
    "1H" to Area("1H", 300f, 18f, 100f, 22f),
    "1I" to Area("1I", 300f, 38f, 100f, 22f),
    "1J" to Area("1J", 300f, 68f, 100f, 22f),
    "1K" to Area("1K", 300f, 98f, 100f, 22f),
    "1L" to Area("1L", 300f, 138f, 100f, 22f),
    "1M" to Area("1M", 300f, 168f, 100f, 22f),
    "1N" to Area("1N", 300f, 198f, 100f, 22f),
    "1O" to Area("1O", 445f, 85f, 70f, 20f),
    "1P" to Area("1P", 445f, 110f, 70f, 20f),
    "1Q" to Area("1Q", 445f, 140f, 70f, 20f),
    "1R" to Area("1R", 445f, 160f,  70f, 20f),
    "1S" to Area("1S", 445f, 220f,  70f, 20f),
    "1T" to Area("1T", 445f, 255f,  70f, 20f),
    "1U" to Area("1U", 445f, 280f,  70f, 20f),
    "1V" to Area("1V", 445f, 310f,  70f, 20f),
    "1W" to Area("1W", 445f, 325f,  70f, 20f),
    "1X" to Area("1X", 445f, 355f,  70f, 20f),
    "1Y" to Area("1Y", 445f, 385f, 70f, 20f),
    "1Z" to Area("1Z", 445f, 405f, 70f, 20f),
    "1AA" to Area("1AA", 445f, 440f, 70f, 20f),
)

const val TERREO_MAP_WIDTH = 624f
const val TERREO_MAP_HEIGHT = 762f
const val SUPERIOR_MAP_WIDTH = 605f
const val SUPERIOR_MAP_HEIGHT = 665f


fun Area.toNormalizedRectF(imageWidth: Float, imageHeight: Float): RectF {


    val left = this.x / imageWidth
    val top = this.y / imageHeight
    val right = (this.x + this.width) / imageWidth
    val bottom = (this.y + this.height) / imageHeight

    return RectF(left, top, right, bottom)
}

fun getShelfLocations(floor: Floor): List<ShelfLocation> {
    return when (floor) {
        Floor.GROUND -> areasTerreo.values.map { area ->
            ShelfLocation(
                shelfCode = area.id,
                floor = Floor.GROUND,
                rect = area.toNormalizedRectF(TERREO_MAP_WIDTH, TERREO_MAP_HEIGHT)
            )
        }
        Floor.UPPER -> areasSuperior.values.map { area ->
            ShelfLocation(
                shelfCode = area.id,
                floor = Floor.UPPER,
                rect = area.toNormalizedRectF(SUPERIOR_MAP_WIDTH, SUPERIOR_MAP_HEIGHT)
            )
        }
    }
}
