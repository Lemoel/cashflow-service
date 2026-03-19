package br.com.cashflow.usecase.lancamento.entity

import br.com.cashflow.usecase.lancamento.enum.MeioCapturaEnum
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class MeioCapturaConverter : AttributeConverter<MeioCapturaEnum, String> {
    override fun convertToDatabaseColumn(attr: MeioCapturaEnum?): String? = attr?.code

    override fun convertToEntityAttribute(code: String?): MeioCapturaEnum = MeioCapturaEnum.fromCode(code)
}
