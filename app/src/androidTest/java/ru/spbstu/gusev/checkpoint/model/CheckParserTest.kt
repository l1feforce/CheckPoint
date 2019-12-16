package ru.spbstu.gusev.checkpoint.model

import com.google.firebase.ml.vision.text.FirebaseVisionText
import org.junit.Assert
import org.junit.Test

class CheckParserTest {
    private val firebaseVisionText = FirebaseVisionText(
    "ЛОБРО ПОЖОЛОВАТЬ!\n" +
    "    КАССОВЫЙ ЧЕК\n" +
    "    ПРИХОД\n" +
    "    ПРОДАХА КОСС19675\n" +
    "    СТКР. 13: 49:47\n" +
    "    Закр. 13:53:1?\n" +
    "    Столик: 78\n" +
    "    ПО СОБЕРИ САN КЗ\n" +
    "    1.000 229.03 =229.00\n" +
    "    ТОВАР\n" +
    "    ПОЛНЫЙ РАСЧЕТ\n" +
    "    БCPК БАЕЧИКИН\n" +
    "    ГИРОС РМЕНИК\n" +
    "    ГИРИГ КЛУБНИКА\n" +
    "    И ЗЕЛЕНЕЙ МЕ МИСА\n" +
    "    Гозилия: 1\n" +
    "    Покупок: 1\n" +
    "    Смма боз СКИАСК.\n" +
    "    229,00\n" +
    "    ИТОГ\n" +
    "    #229.00\n" +
    "    Сннма беа НаС\n" +
    "    ЭЛЕК ГРОННЫМИ\n" +
    "    ПЛАТ КАРТОР\n" +
    "    ПОЛУЧЕНО\n" +
    "    CHO:\n" +
    "    Пользователь :\n" +
    "    -229,00\n" +
    "    3225.aЙ\n" +
    "    -229.UC\n" +
    "    #229.UU\n" +
    "    УСН АСХОД\n" +
    "    Инди\n" +
    "    ВИАУАЛЬНЫЙ ПРедПРИНИма тль КгРИИАН ПЛЕГ ИТСРЕВИ\n" +
    "    78-Г.Сакт-Пeте\n" +
    "    АДрес:\n" +
    "    рбУРГ, Уд. ПолИтехни-аская, д. 31. Пит.Х, псм.411\n" +
    "    каче \"ПиРОговнй ДВРАК\"\n" +
    "    Порсаева Снана феликсовна\n" +
    "    Mесто расчетов:\n" +
    "    Кассир:\n" +
    "    Сайт ФHС:\n" +
    "    +ЗН ККТ:\n" +
    "    Смена К\n" +
    "    Чек К\n" +
    "    Дата ВРемя\n" +
    "    ОPА:\n" +
    "    ИНН:\n" +
    "    PH KKT:\n" +
    "    PH N\n" +
    "    9A N\n" +
    "    Ф:\n" +
    "    juu.natog.ru\n" +
    "    00106909838798\n" +
    "    C0177\n" +
    "    08129\n" +
    "    15.11.19 13:51\n" +
    "    000 <ЛЕТЕР-СЕРВИС Спецтехнологии>\n" +
    "    510100413398\n" +
    "    ОВО0150841849501\n" +
    "    9252440300119281\n" +
    "    0000038125\n" +
    "    2656809920\n" +
    "    СПАСИБО\n" +
    "    ЗА ПОКЧЕКУ?", listOf()
    )
    @Test
    fun testParseTextDate() {
        val qr = ""
        val defaultDate = "12.12.1754"
        val parser = CheckParser
        parser.parsedCheck.date = defaultDate
        val result = CheckParser.parse(firebaseVisionText, qr)
        Assert.assertEquals("15.11.19", result.date)
    }

    @Test
    fun testParseSum() {
        val qr = ""
        val defaultPrice = 0f
        val parser = CheckParser
        parser.parsedCheck.finalPrice = defaultPrice
        val result = CheckParser.parse(firebaseVisionText, qr)
        Assert.assertEquals(229.03f, result.finalPrice)
    }

    @Test
    fun testParseQr() {
        val qr = "t=20181007T2151&s=1955.49&fn=8710000101838052&i=18487&fp=2392195712&n=1\n"
        val result = CheckParser.parse(firebaseVisionText, qr)
        Assert.assertEquals("2018.10.07", result.date)
        Assert.assertEquals(1955.49f, result.finalPrice)
    }

    @Test
    fun testEmptyPhoto() {
        val qr = ""
        val image = FirebaseVisionText("", listOf())
        val result = CheckParser.parse(image, qr)
    }
}