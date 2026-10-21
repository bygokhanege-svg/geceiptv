package app.opentv

import app.opentv.data.repo.expandQueryVariants
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CatalogSearchTest {

    @Test
    fun `empty and blank queries return empty variant list`() {
        assertThat(expandQueryVariants("")).isEqualTo(listOf("", "", "", ""))
        assertThat(expandQueryVariants("   ")).isEqualTo(listOf("", "", "", ""))
    }

    @Test
    fun `cukur ascii input expands to Turkish ÇUKUR and variations`() {
        val variants = expandQueryVariants("cukur")
        assertThat(variants).contains("cukur")
        assertThat(variants).contains("CUKUR")
        assertThat(variants).contains("ÇUKUR")
    }

    @Test
    fun `çukur turkish input expands to uppercase ÇUKUR and ASCII CUKUR`() {
        val variants = expandQueryVariants("çukur")
        assertThat(variants).contains("çukur")
        assertThat(variants).contains("ÇUKUR")
        assertThat(variants).contains("CUKUR")
    }

    @Test
    fun `inci input expands to Turkish uppercase İNCİ`() {
        val variants = expandQueryVariants("inci")
        assertThat(variants).contains("inci")
        assertThat(variants).contains("INCI")
        assertThat(variants).contains("İNCİ")
    }

    @Test
    fun `gonul ascii input expands to Turkish uppercase GÖNÜL`() {
        val variants = expandQueryVariants("gonul")
        assertThat(variants).contains("gonul")
        assertThat(variants).contains("GONUL")
        assertThat(variants).contains("GÖNÜL")
    }

    @Test
    fun `gönül turkish input expands to uppercase GÖNÜL and ascii GONUL`() {
        val variants = expandQueryVariants("gönül")
        assertThat(variants).contains("gönül")
        assertThat(variants).contains("GÖNÜL")
        assertThat(variants).contains("GONUL")
    }

    @Test
    fun `kızılcık turkish input expands to uppercase KIZILCIK and ascii`() {
        val variants = expandQueryVariants("kızılcık")
        assertThat(variants).contains("kızılcık")
        assertThat(variants).contains("KIZILCIK")
    }

    @Test
    fun `serbet ascii input expands to uppercase ŞERBET`() {
        val variants = expandQueryVariants("serbet")
        assertThat(variants).contains("serbet")
        assertThat(variants).contains("ŞERBET")
    }
}
