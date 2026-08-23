add demo to show that the rendered timezone of `OffsetDateTime`, `OffsetTime` and `ZonedDateTime` can be controlled using `@ValueSemantics(timeZoneTranslation = TimeZoneTranslation.NONE)`

eg

    @Property
    @ValueSemantics(timeZoneTranslation = TimeZoneTranslation.NONE)
    @PropertyLayout(
            describedAs = "@ValueSemantics(timeZoneTranslation = TimeZoneTranslation.NONE)",
            labelPosition = LabelPosition.TOP,
            hidden = Where.ALL_TABLES,
            fieldSetId = "time-zone-translation", sequence = "1")
    default java.time.OffsetDateTime getReadOnlyPropertyNoTimeZoneTranslation() {
        return getReadOnlyProperty();
    }
