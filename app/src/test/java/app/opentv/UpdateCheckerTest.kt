package app.opentv

import app.opentv.update.UpdateChecker
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun `isNewer correctly compares version tags`() {
        assertThat(UpdateChecker.isNewer("v0.12.10", "0.12.09")).isTrue()
        assertThat(UpdateChecker.isNewer("v0.13.00", "0.12.09")).isTrue()
        assertThat(UpdateChecker.isNewer("v1.0.0", "0.12.09")).isTrue()
        assertThat(UpdateChecker.isNewer("v0.12.09", "0.12.09")).isFalse()
        assertThat(UpdateChecker.isNewer("v0.12.08", "0.12.09")).isFalse()
    }

    @Test
    fun `repairs turkish mojibake in update notes`() {
        val corrupted = "SÄ±fÄ±r Gecikmeli UDP Ä°letiÅŸimi, TÃ¼rkÃ§e SaÄŸlayÄ±cÄ± ve Ã‡oklu Dil"
        val repaired = UpdateChecker.fixMojibake(corrupted)

        assertThat(repaired).isEqualTo("Sıfır Gecikmeli UDP İletişimi, Türkçe Sağlayıcı ve Çoklu Dil")
    }

    @Test
    fun `cleanNotes strips markdown and fixes turkish characters`() {
        val raw = """
            ## GECE IPTV v0.12.09 Yenilikleri
            - **SÄ±fÄ±r Gecikme:** TV ekranÄ± Ã¼zerinden hÄ±zlÄ± geÃ§iÅŸ.
            - **Ä°letiÅŸim:** geceiptv@gmail.com
            
            Built by GitHub Actions
            **Full Changelog**: https://github.com/foo/bar
        """.trimIndent()

        val cleaned = UpdateChecker.cleanNotes(raw)

        assertThat(cleaned).contains("GECE IPTV v0.12.09 Yenilikleri")
        assertThat(cleaned).contains("• Sıfır Gecikme: TV ekranı üzerinden hızlı geçiş.")
        assertThat(cleaned).contains("• İletişim: geceiptv@gmail.com")
        assertThat(cleaned).doesNotContain("Built by GitHub Actions")
        assertThat(cleaned).doesNotContain("Full Changelog")
    }
}
