package br.com.cashflow.usecase.lancamento.entity

import br.com.cashflow.usecase.lancamento.enum.MeioPagamentoEnum
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class MeioPagamentoConverter : AttributeConverter<MeioPagamentoEnum, String> {
    override fun convertToDatabaseColumn(attr: MeioPagamentoEnum?): String? = attr?.code

    override fun convertToEntityAttribute(code: String?): MeioPagamentoEnum = MeioPagamentoEnum.fromCode(code)
}
