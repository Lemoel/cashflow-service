package br.com.cashflow.usecase.lancamento.entity

import br.com.cashflow.usecase.lancamento.enum.TipoEventoEnum
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class TipoEventoConverter : AttributeConverter<TipoEventoEnum, String> {
    override fun convertToDatabaseColumn(attr: TipoEventoEnum?): String? = attr?.code

    override fun convertToEntityAttribute(code: String?): TipoEventoEnum = TipoEventoEnum.fromCode(code)
}
