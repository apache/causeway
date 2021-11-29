package org.apache.isis.core.runtimeservices.command;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.jaxb.JavaTimeXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.schema.SchemaValueMarshaller;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.semantics.Converter;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.schema.cmd.v2.ParamDto;
import org.apache.isis.schema.cmd.v2.PropertyDto;
import org.apache.isis.schema.common.v2.BlobDto;
import org.apache.isis.schema.common.v2.ClobDto;
import org.apache.isis.schema.common.v2.CollectionDto;
import org.apache.isis.schema.common.v2.EnumDto;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.common.v2.TypedTupleDto;
import org.apache.isis.schema.common.v2.ValueDto;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;

@Service
@Named("isis.runtimeservices.SchemaValueMarshallerDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor
public class SchemaValueMarshallerDefault implements SchemaValueMarshaller {

    @Inject private BookmarkService bookmarkService;
    @Inject private ValueSemanticsResolver valueSemanticsResolver;
    @Inject private SpecificationLoader specLoader;

    // -- RECOVER VALUES FROM DTO

    @Override
    public Object recoverValueFrom(final PropertyDto propertyDto) {
        final ValueWithTypeDto valueWithTypeDto = propertyDto.getNewValue();
        return recoverValue(valueWithTypeDto, this);
    }

    @Override
    public Object recoverValueFrom(
            final Identifier paramIdentifier,
            final ParamDto paramDto) {
        return recoverValue(paramDto, this);
    }

    // -- RECORD VALUES INTO DTO

    @Override
    public ActionInvocationDto recordActionResult(
            final ActionInvocationDto invocationDto,
            final Class<?> returnType,
            final Object result) {
        final ValueTypeWrapper<?> valueTypeAndSemantics = resolve(this, returnType);
        final ValueWithTypeDto returned = newValueWithTypeDto(valueTypeAndSemantics, result, this);
        invocationDto.setReturned(returned);
        return invocationDto;
    }

    @Override
    public PropertyDto recordPropertyValue(
            final PropertyDto propertyDto,
            final Class<?> propertyType,
            final Object valuePojo) {
        final ValueTypeWrapper<?> valueTypeAndSemantics = resolve(this, propertyType);
        final ValueWithTypeDto newValue = newValueWithTypeDto(valueTypeAndSemantics, valuePojo, this);
        propertyDto.setNewValue(newValue);
        return propertyDto;
    }

    @Override
    public ParamDto recordParamValue(
            final Identifier paramIdentifier,
            final ParamDto paramDto,
            final Class<?> paramType,
            final Object valuePojo) {

        //specLoader.loadFeature(null);

//        val paramDto = actionParameter.getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION
//                ? valueMarshaller.newParamDtoNonScalar(
//                        actionParameter.getStaticFriendlyName()
//                            .orElseThrow(_Exceptions::unexpectedCodeReach),
//                        paramTypeOrElementType,
//                        arg)
//                : valueMarshaller.newParamDtoScalar(
//                        actionParameter.getStaticFriendlyName()
//                            .orElseThrow(_Exceptions::unexpectedCodeReach),
//                        paramTypeOrElementType,
//                        arg);
        // TODO Auto-generated method stub
        return null;
    }


    private ParamDto newParamDtoScalar(
            final String parameterName,
            final Class<?> paramType,
            final Object valuePojo) {
        val paramDto = new ParamDto();
        paramDto.setName(parameterName);

        final ValueTypeWrapper<?> valueTypeAndSemantics = resolve(this, paramType);

//        ValueType valueType = valueTypeAndSemantics.getValueType();
//
//        // this hack preserves previous behaviour before we were able to serialize blobs and clobs into XML
//        // however, we also don't want this new behaviour for parameter arguments
//        // (else these large objects could end up being persisted).
//        if(valueType == ValueType.BLOB) valueType = ValueType.REFERENCE;
//        if(valueType == ValueType.CLOB) valueType = ValueType.REFERENCE;

        paramDto.setType(valueTypeAndSemantics.getValueType());

        recordValue(paramDto, valueTypeAndSemantics, valuePojo, this);

        return paramDto;
    }


    private ParamDto newParamDtoNonScalar(
            final String parameterName,
            final Class<?> paramElementType,
            final Object valuePojo) {

        val paramDto = new ParamDto();
        paramDto.setName(parameterName);
        paramDto.setType(ValueType.COLLECTION);

        ValueTypeWrapper<?> elementValueTypeAndSemantics = resolve(this, paramElementType);

        setValueOnNonScalar(paramDto, elementValueTypeAndSemantics, valuePojo, this);

        return paramDto;

    }

    // -- HELPER

    @Value(staticConstructor = "of")
    private static class ValueTypeWrapper<T> {
        final @NonNull ValueType valueType;
        final @Nullable ValueSemanticsProvider<T> semantics;

        private final static ValueTypeWrapper<Void> VOID = ValueTypeWrapper.of(ValueType.VOID, null);
        public static ValueTypeWrapper<Void> empty() {
            return VOID;
        }

        public Converter<T, ?> getConverter() {
            return semantics!=null
                    ? semantics.getConverter()
                    : null;
        }

    }

    private ValueTypeWrapper<?> resolve(
            @NonNull final SchemaValueMarshaller valueMarshaller,
            @NonNull final Class<?> type) {
        return valueSemanticsResolver.selectValueSemantics(type)
        .getFirst()
        .map(valueSemantics->ValueTypeWrapper.of(valueSemantics.getSchemaValueType(), valueSemantics))
        // assume reference otherwise
        .orElseGet(()->ValueTypeWrapper.of(ValueType.REFERENCE, null));
    }

    private ValueTypeWrapper<?> resolve(
            @NonNull final SchemaValueMarshaller valueMarshaller,
            //@NonNull final Class<?> type,
            final ValueWithTypeDto valueWithTypeDto) {
        // TODO Auto-generated method stub

        return ValueTypeWrapper.of(valueWithTypeDto.getType(), null);
    }

    private ValueWithTypeDto newValueWithTypeDto(
            final ValueTypeWrapper<?> valueTypeAndSemantics,
            final Object valuePojo,
            final @NonNull SchemaValueMarshaller valueMarshaller) {

        final ValueWithTypeDto valueWithTypeDto = new ValueWithTypeDto();
        recordValue(valueWithTypeDto, valueTypeAndSemantics, valuePojo, valueMarshaller);

        return valueWithTypeDto;
    }

    private <T extends ValueDto> T recordValue(
            final T valueDto,
            final ValueTypeWrapper<?> valueTypeAndSemantics,
            final Object pojo,
            final @NonNull SchemaValueMarshaller valueMarshaller) {

        val valueType = valueTypeAndSemantics.getValueType();
        val semantics = valueTypeAndSemantics.getSemantics();
        val converter = valueTypeAndSemantics.getConverter();

        switch (valueType) {
        case COLLECTION: {
            final CollectionDto collectionDto = asCollectionDto(
                    pojo, ValueTypeWrapper.empty(), valueMarshaller);
            valueDto.setCollection(collectionDto);
            return valueDto;
        }
        case COMPOSITE: {
            final TypedTupleDto typedTupleDto = asTypedTupleDto(pojo, valueMarshaller);
            valueDto.setComposite(typedTupleDto);
            return valueDto;
        }
        case STRING: {
            final String argValue = (String) pojo;
            valueDto.setString(argValue);
            return valueDto;
        }
        case BYTE: {
            final Byte argValue = (Byte) pojo;
            valueDto.setByte(argValue);
            return valueDto;
        }
        case SHORT: {
            final Short argValue = (Short) pojo;
            valueDto.setShort(argValue);
            return valueDto;
        }
        case INT: {
            final Integer argValue = (Integer) pojo;
            valueDto.setInt(argValue);
            return valueDto;
        }
        case LONG: {
            final Long argValue = (Long) pojo;
            valueDto.setLong(argValue);
            return valueDto;
        }
        case CHAR: {
            final Character argValue = (Character) pojo;
            valueDto.setChar("" + argValue);
            return valueDto;
        }
        case BOOLEAN: {
            final Boolean argValue = (Boolean) pojo;
            valueDto.setBoolean(argValue);
            return valueDto;
        }
        case FLOAT: {
            final Float argValue = (Float) pojo;
            valueDto.setFloat(argValue);
            return valueDto;
        }
        case DOUBLE: {
            final Double argValue = (Double) pojo;
            valueDto.setDouble(argValue);
            return valueDto;
        }
        case BIG_INTEGER: {
            final BigInteger argValue = (BigInteger) pojo;
            valueDto.setBigInteger(argValue);
            return valueDto;
        }
        case BIG_DECIMAL: {
            final BigDecimal argValue = (BigDecimal) pojo;
            valueDto.setBigDecimal(argValue);
            return valueDto;
        }
        case LOCAL_DATE: {
            final LocalDate argValue = (LocalDate) pojo;
            valueDto.setLocalDate(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case LOCAL_TIME: {
            final LocalTime argValue = (LocalTime) pojo;
            valueDto.setLocalTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case LOCAL_DATE_TIME: {
            final LocalDateTime argValue = converter!=null
                    ? (LocalDateTime) converter.toDelegateValue(_Casts.uncheckedCast(pojo))
                    : (LocalDateTime) pojo;
            valueDto.setLocalDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case OFFSET_DATE_TIME: {
            final OffsetDateTime argValue = (OffsetDateTime) pojo;
            valueDto.setOffsetDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case OFFSET_TIME: {
            final OffsetTime argValue = (OffsetTime) pojo;
            valueDto.setOffsetTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case ZONED_DATE_TIME: {
            final ZonedDateTime argValue = (ZonedDateTime) pojo;
            valueDto.setZonedDateTime(JavaTimeXMLGregorianCalendarMarshalling.toXMLGregorianCalendar(argValue));
            return valueDto;
        }
        case ENUM: {
            final Enum<?> argValue = (Enum<?>) pojo;
            if(argValue == null) {
                return null;
            }
            final EnumDto enumDto = new EnumDto();
            valueDto.setEnum(enumDto);
            enumDto.setEnumType(argValue.getClass().getName());
            enumDto.setEnumName(argValue.name());
            return valueDto;
        }
        case REFERENCE: {
            final Bookmark bookmark = pojo instanceof Bookmark
                    ? (Bookmark) pojo
                    : bookmarkService!=null
                            ? bookmarkService.bookmarkFor(pojo).orElse(null)
                            : null;

            if (bookmark != null) {
                OidDto argValue = bookmark.toOidDto();
                valueDto.setReference(argValue);
            }
            return valueDto;
        }
        case BLOB: {
            final Blob blob = (Blob) pojo;
            if(blob != null) {
                final BlobDto blobDto = new BlobDto();
                blobDto.setName(blob.getName());
                blobDto.setBytes(blob.getBytes());
                blobDto.setMimeType(blob.getMimeType().toString());
                valueDto.setBlob(blobDto);
            }
            return valueDto;
        }
        case CLOB: {
            final Clob clob = (Clob) pojo;
            if(clob != null) {
                final ClobDto clobDto = new ClobDto();
                clobDto.setName(clob.getName());
                clobDto.setChars(clob.getChars().toString());
                clobDto.setMimeType(clob.getMimeType().toString());
                valueDto.setClob(clobDto);
            }
            return valueDto;
        }
        case VOID: {
            return null;
        }
        default:
            throw _Exceptions.unmatchedCase(valueType);
        }
    }

    @Deprecated
    private <T extends ValueWithTypeDto> T setValueOnNonScalar(
            final T valueWithTypeDto,
            final ValueTypeWrapper<?> elementValueTypeAndSemantics,
            final Object value,
            final @NonNull SchemaValueMarshaller valueMarshaller) {

        valueWithTypeDto.setType(ValueType.COLLECTION);

        val collectionDto = asCollectionDto(value, elementValueTypeAndSemantics, valueMarshaller);
        valueWithTypeDto.setCollection(collectionDto);
        valueWithTypeDto.setNull(value == null);

        return valueWithTypeDto;
    }

    private CollectionDto asCollectionDto(
            final @Nullable Object iterableOrArray,
            final @NonNull ValueTypeWrapper<?> commonElementValueTypeAndSemantics,
            final @NonNull SchemaValueMarshaller valueMarshaller) {

        val commonElementValueType = commonElementValueTypeAndSemantics.getValueType();
        val collectionDto = new CollectionDto();
        collectionDto.setType(commonElementValueType);

        val needsCommonElementValueTypeAutodetect = commonElementValueType==ValueType.VOID;

        val commonElementValueTypeRef = _Refs.<ValueType>objectRef(null);

        _NullSafe.streamAutodetect(iterableOrArray)
        .forEach(element->{
            val valueDto = new ValueDto();
            if(element==null) {
                recordValue(valueDto, ValueTypeWrapper.empty(), element, valueMarshaller);
            } else {
                ValueTypeWrapper<?> elementValueTypeAndSemantics =
                        resolve(valueMarshaller, element.getClass());
                recordValue(valueDto, elementValueTypeAndSemantics, element, valueMarshaller);

                if(needsCommonElementValueTypeAutodetect) {
                    commonElementValueTypeRef.update(acc->reduce(acc, elementValueTypeAndSemantics.getValueType()));
                }

            }
            collectionDto.getValue().add(valueDto);
        });

        if(needsCommonElementValueTypeAutodetect) {
            collectionDto.setType(commonElementValueTypeRef.getValueElseDefault(ValueType.VOID));
        }

        return collectionDto;
    }

    private static ValueType reduce(final ValueType acc, final ValueType next) {
        if(acc==null) {
            return next;
        }
        if(acc==next) {
            return acc;
        }
        throw _Exceptions.unsupportedOperation("mixing types within a collection is not supported yet");
    }

    private static TypedTupleDto asTypedTupleDto(
            final @Nullable Object composite,
            final @NonNull SchemaValueMarshaller valueMarshaller) {
        val typedTupleDto = new TypedTupleDto();
        //TODO implement
        return typedTupleDto;
    }

    // -- HELPER - RECOVERY

    private <T> T recoverValue(
            final ValueWithTypeDto valueWithTypeDto,
            final @NonNull SchemaValueMarshaller valueMarshaller) {
        if(valueWithTypeDto==null
                || (valueWithTypeDto.isSetNull()
                    && valueWithTypeDto.isNull())) {
            return null;
        }
        return recoverValue(valueWithTypeDto, resolve(valueMarshaller, valueWithTypeDto));
    }

    private static <T> T recoverValue(
            final ValueDto valueDto,
            final @NonNull ValueTypeWrapper<?> valueTypeAndSemantics) {
        return _Casts.uncheckedCast(recoverValueAsObject(valueDto, valueTypeAndSemantics));
    }

    @SneakyThrows
    private static Object recoverValueAsObject(
            final ValueDto valueDto,
            final ValueTypeWrapper<?> valueTypeAndSemantics) {

        val valueType = valueTypeAndSemantics.getValueType();
        //val semantics = valueTypeAndSemantics.getSemantics();
        //val converter = valueTypeAndSemantics.getConverter();

        switch(valueType) {
        case STRING:
            return valueDto.getString();
        case BYTE:
            return valueDto.getByte();
        case SHORT:
            return valueDto.getShort();
        case INT:
            return valueDto.getInt();
        case LONG:
            return valueDto.getLong();
        case FLOAT:
            return valueDto.getFloat();
        case DOUBLE:
            return valueDto.getDouble();
        case BOOLEAN:
            return valueDto.isBoolean();
        case CHAR:
            final String aChar = valueDto.getChar();
            if(_Strings.isNullOrEmpty(aChar)) { return null; }
            return aChar.charAt(0);
        case BIG_DECIMAL:
            return valueDto.getBigDecimal();
        case BIG_INTEGER:
            return valueDto.getBigInteger();
        case LOCAL_DATE:
            return JavaTimeXMLGregorianCalendarMarshalling.toLocalDate(valueDto.getLocalDate());
        case LOCAL_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toLocalTime(valueDto.getLocalTime());
        case LOCAL_DATE_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toLocalDateTime(valueDto.getLocalDateTime());
        case OFFSET_DATE_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toOffsetDateTime(valueDto.getOffsetDateTime());
        case OFFSET_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toOffsetTime(valueDto.getOffsetTime());
        case ZONED_DATE_TIME:
            return JavaTimeXMLGregorianCalendarMarshalling.toZonedDateTime(valueDto.getZonedDateTime());
        case ENUM:
            final EnumDto enumDto = valueDto.getEnum();
            final String enumType = enumDto.getEnumType();
            @SuppressWarnings("rawtypes")
            final Class<? extends Enum> enumClass =
                    _Casts.uncheckedCast(_Context.loadClassAndInitialize(enumType));
            return Enum.valueOf(_Casts.uncheckedCast(enumClass), enumDto.getEnumName());
        case REFERENCE:
            return valueDto.getReference();
        case COLLECTION:
            val collectionDto = valueDto.getCollection();
            if(_NullSafe.isEmpty(collectionDto.getValue())) {
                return Collections.emptyList();
            }
            val list = new ArrayList<Object>();

            val elementValueType = collectionDto.getType();

            for(val elementValueDto : collectionDto.getValue()) {

                //FIXME[ISIS-2877]
//                if(elementValueDto instanceof ValueWithTypeDto) {
//                    list.add(getValueAsObject(elementValueDto, ((ValueWithTypeDto)elementValueDto).getType()));
//                } else {
//                    list.add(getValueAsObject(elementValueDto, elementValueType));
//                }

            }
            return list;
        case BLOB:
            final BlobDto blobDto = valueDto.getBlob();
            return new Blob(blobDto.getName(), blobDto.getMimeType(), blobDto.getBytes());
        case CLOB:
            final ClobDto clobDto = valueDto.getClob();
            return new Clob(clobDto.getName(), clobDto.getMimeType(), clobDto.getChars());
        case VOID:
            return null;
        default:
            throw _Exceptions.unmatchedCase(valueType);
        }
    }

}
