package com.opensound.app.data

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType

object AtmosphereConfigSerializer {
    private const val FormatVersion = "1"

    fun encode(config: AtmosphereConfig): String {
        val values = mutableListOf<Pair<String, String>>()

        values.add("version" to FormatVersion)
        values.add("characterX" to config.characterX.toString())
        values.add("characterY" to config.characterY.toString())
        values.add("characterSize" to config.characterSize.toString())
        values.add("accentColor" to config.accentColor.toString())
        values.add("glowIntensity" to config.glowIntensity.toString())
        values.add("panelOpacity" to config.panelOpacity.toString())
        values.add("presetName" to StorageTextCodec.encode(config.presetName))
        values.add("overlayText" to StorageTextCodec.encode(config.overlayText))
        values.add("overlayTextX" to config.overlayTextX.toString())
        values.add("overlayTextY" to config.overlayTextY.toString())
        values.add("overlayTextStart" to config.overlayTextStart.toString())
        values.add("overlayTextEnd" to config.overlayTextEnd.toString())
        values.add("overlayTextAnimation" to StorageTextCodec.encode(config.overlayTextAnimation))
        values.add("layers.count" to config.layers.size.toString())

        config.layers.forEachIndexed { index, layer ->
            values.addLayer(index = index, layer = layer)
        }

        return values.joinToString(separator = "\n") { (key, value) ->
            "$key=$value"
        }
    }

    fun decode(value: String): AtmosphereConfig? {
        val values = parseValues(value)
        if (values["version"] != FormatVersion) return null

        val defaults = AtmosphereConfig()

        return defaults.copy(
            characterX = values.floatValue("characterX", defaults.characterX),
            characterY = values.floatValue("characterY", defaults.characterY),
            characterSize = values.floatValue("characterSize", defaults.characterSize),
            accentColor = values.longValue("accentColor", defaults.accentColor),
            glowIntensity = values.floatValue("glowIntensity", defaults.glowIntensity),
            panelOpacity = values.floatValue("panelOpacity", defaults.panelOpacity),
            presetName = values.stringValue("presetName", defaults.presetName),
            overlayText = values.stringValue("overlayText", defaults.overlayText),
            overlayTextX = values.floatValue("overlayTextX", defaults.overlayTextX),
            overlayTextY = values.floatValue("overlayTextY", defaults.overlayTextY),
            overlayTextStart = values.floatValue("overlayTextStart", defaults.overlayTextStart),
            overlayTextEnd = values.floatValue("overlayTextEnd", defaults.overlayTextEnd),
            overlayTextAnimation = values.stringValue(
                key = "overlayTextAnimation",
                defaultValue = defaults.overlayTextAnimation
            ),
            layers = values.layers(defaults.layers)
        )
    }

    private fun MutableList<Pair<String, String>>.addLayer(
        index: Int,
        layer: AtmosphereLayer
    ) {
        val prefix = "layer.$index"

        add("$prefix.id" to StorageTextCodec.encode(layer.id))
        add("$prefix.type" to layer.type.name)
        add("$prefix.name" to StorageTextCodec.encode(layer.name))
        add("$prefix.startTime" to layer.startTime.toString())
        add("$prefix.endTime" to layer.endTime.toString())
        add("$prefix.assetRef" to StorageTextCodec.encode(layer.assetRef))
        add("$prefix.x" to layer.x.toString())
        add("$prefix.y" to layer.y.toString())
        add("$prefix.scale" to layer.scale.toString())
        add("$prefix.opacity" to layer.opacity.toString())
        add("$prefix.text" to StorageTextCodec.encode(layer.text))
        add("$prefix.accentColor" to layer.accentColor.toString())
        add("$prefix.animationIn" to StorageTextCodec.encode(layer.animationIn))
        add("$prefix.animationOut" to StorageTextCodec.encode(layer.animationOut))
        add("$prefix.isVisible" to layer.isVisible.toString())
    }

    private fun parseValues(value: String): Map<String, String> {
        return value
            .lineSequence()
            .mapNotNull { line ->
                val separatorIndex = line.indexOf('=')
                if (separatorIndex == -1) {
                    null
                } else {
                    line.substring(0, separatorIndex) to line.substring(separatorIndex + 1)
                }
            }
            .toMap()
    }

    private fun Map<String, String>.layers(defaultLayers: List<AtmosphereLayer>): List<AtmosphereLayer> {
        val count = this["layers.count"]?.toIntOrNull() ?: return defaultLayers

        return (0 until count).map { index ->
            layer(
                index = index,
                defaultLayer = defaultLayers.getOrNull(index) ?: AtmosphereLayer(
                    id = "layer-$index",
                    type = AtmosphereLayerType.Effect,
                    name = "Layer $index",
                    startTime = 0f,
                    endTime = 2f
                )
            )
        }
    }

    private fun Map<String, String>.layer(
        index: Int,
        defaultLayer: AtmosphereLayer
    ): AtmosphereLayer {
        val prefix = "layer.$index"

        return defaultLayer.copy(
            id = stringValue("$prefix.id", defaultLayer.id),
            type = layerTypeValue("$prefix.type", defaultLayer.type),
            name = stringValue("$prefix.name", defaultLayer.name),
            startTime = floatValue("$prefix.startTime", defaultLayer.startTime),
            endTime = floatValue("$prefix.endTime", defaultLayer.endTime),
            assetRef = stringValue("$prefix.assetRef", defaultLayer.assetRef),
            x = floatValue("$prefix.x", defaultLayer.x),
            y = floatValue("$prefix.y", defaultLayer.y),
            scale = floatValue("$prefix.scale", defaultLayer.scale),
            opacity = floatValue("$prefix.opacity", defaultLayer.opacity),
            text = stringValue("$prefix.text", defaultLayer.text),
            accentColor = longValue("$prefix.accentColor", defaultLayer.accentColor),
            animationIn = stringValue("$prefix.animationIn", defaultLayer.animationIn),
            animationOut = stringValue("$prefix.animationOut", defaultLayer.animationOut),
            isVisible = booleanValue("$prefix.isVisible", defaultLayer.isVisible)
        )
    }

    private fun Map<String, String>.floatValue(
        key: String,
        defaultValue: Float
    ): Float {
        return this[key]?.toFloatOrNull() ?: defaultValue
    }

    private fun Map<String, String>.longValue(
        key: String,
        defaultValue: Long
    ): Long {
        return this[key]?.toLongOrNull() ?: defaultValue
    }

    private fun Map<String, String>.stringValue(
        key: String,
        defaultValue: String
    ): String {
        return this[key]?.let(StorageTextCodec::decode) ?: defaultValue
    }

    private fun Map<String, String>.booleanValue(
        key: String,
        defaultValue: Boolean
    ): Boolean {
        return when (this[key]) {
            "true" -> true
            "false" -> false
            else -> defaultValue
        }
    }

    private fun Map<String, String>.layerTypeValue(
        key: String,
        defaultValue: AtmosphereLayerType
    ): AtmosphereLayerType {
        return this[key]
            ?.let { value ->
                runCatching { AtmosphereLayerType.valueOf(value) }.getOrNull()
            }
            ?: defaultValue
    }
}
