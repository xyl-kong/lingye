package com.example.tabletworkspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LocalizationTest {
 @Test fun languageCodesAreStableAndUnknownValuesFallBackToChinese() {
  assertEquals(AppLanguage.ZH, AppLanguage.fromCode(null))
  assertEquals(AppLanguage.ZH, AppLanguage.fromCode("zh"))
  assertEquals(AppLanguage.EN, AppLanguage.fromCode("EN"))
  assertEquals(AppLanguage.ZH, AppLanguage.fromCode("unsupported"))
  assertEquals(AppLanguage.EN, AppLanguage.ZH.toggle())
  assertEquals("en", AppLanguage.EN.code)
 }

 @Test fun standardDatabaseStatusesAndKnownErrorsSwitchLanguage() {
  assertEquals("To do", statusLabel(AppLanguage.EN, "待办"))
  assertEquals("In progress", statusLabel(AppLanguage.EN, "进行中"))
  assertEquals("Done", statusLabel(AppLanguage.EN, "完成"))
  assertEquals("Custom", statusLabel(AppLanguage.EN, "Custom"))
  assertEquals("待办", statusValue(AppLanguage.EN, "To do"))
  assertEquals("进行中", statusValue(AppLanguage.EN, "In progress"))
  assertEquals("完成", statusValue(AppLanguage.EN, "Done"))
  assertEquals("Custom", statusValue(AppLanguage.EN, "Custom"))
  assertEquals("Image exceeds 20 MB", localizedError(AppLanguage.EN, IllegalArgumentException("图片超过 20 MB")))
  assertEquals("自定义错误", localizedError(AppLanguage.ZH, IllegalArgumentException("自定义错误")))
 }

 @Test fun builtInCardPresetsFollowUiLanguageWithoutChangingStableKind() {
  assertEquals(CardPreset.TODO, CardPreset.fromLegacyName("待办"))
  val zh = CardPreset.TODO.content(AppLanguage.ZH)
  val en = CardPreset.TODO.content(AppLanguage.EN)
  assertEquals("待办", zh.title)
  assertEquals("To-do", en.title)
  assertEquals("☐ 待办事项", zh.body)
  assertEquals("☐ To-do item", en.body)
  assertNotEquals(zh.body, en.body)
 }
}
