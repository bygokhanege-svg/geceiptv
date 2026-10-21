# GECE IPTV - Konuşma, İstek ve Geliştirme Geçmişi (Conversation & Decision Log)

> **Önemli Kural (AI Ajanları İçin):** 
> Yeni bir istek veya güncelleme geldiğinde gereksiz token harcamamak için öncelikle bu dosyayı inceleyin. Kullanıcının daha önce sorduğu sorular, aldığı kararlar, mimari tercihler ve versiyon detayları burada kayıtlıdır.

---

## 📋 Kronolojik İstek ve Karar Kayıtları

### 1. TV ve Telefon Eşleştirme (QR Web Portalı)
* **Kullanıcı İstekleri:**
  * TV ile telefon senkronizasyonunun QR menüsü üzerinden nasıl sağlanacağına dair yol haritası.
  * Giriş ekranındaki otomatik yüklenen varsayılan M3U listesinin yasal riskleri önlemek amacıyla iptal edilmesi; kullanıcıların yalnızca kendi listelerini girmesi.
* **Teknik Çözüm ve Kararlar:**
  * Yerel HTTP/WebSocket sunucusu (`ManagerServer.kt`) TV üzerinde başlatıldı.
  * TV ekranında QR kod ve kısa yerel IP adresi oluşturuldu.
  * Web portalı (`web-portal/index.html`) üzerinden telefondan kumanda, kanal gezinme, IPTV ekleme ve TV'ye aktarım sağlandı.
  * Varsayılan M3U kaldırılarak temiz başlangıç sağlandı.

---

### 2. Çoklu Ekran (Multi-View) ve D-Pad Kumanda İyileştirmeleri
* **Kullanıcı İstekleri:**
  * Çoklu kanal izleme ekranında kanal açıldıktan sonra OK basılınca açılan harici menüye kumandadan ulaşılamıyordu. Bu menünün seçili kanal karesinin içinde açılması ve doğrudan kumanda D-Pad ile kontrol edilebilmesi istendi.
  * Kanal değiştir ve tam ekran tuşlarının aktif hale getirilmesi.
* **Teknik Çözüm ve Kararlar:**
  * Çoklu ekran kontrol kutusu odaklanmış aktif slotun içerisine taşındı.
  * Tam ekran, slot kapatma, kanal değiştirme ve ses açma butonları D-pad odaklanabilir hale getirildi.

---

### 3. Video Oynatıcı Kalite Seçenekleri ve Performans
* **Kullanıcı İstekleri:**
  * Oynatıcıda 360p gibi düşük kaliteye çekince değişim olmaması üzerine sunucu tarafı ve lag/donma etkisi soruldu.
  * Sunucudan gelen tekil yayınlarda gereksiz çözünürlük düşürme seçeneklerinin kaldırılması, sadece sunucudan çoklu kalite akışı geliyorsa seçenek sunulması istendi.
* **Teknik Çözüm ve Kararlar:**
  * Yapay çözünürlük düşürme menüsü kaldırıldı.
  * ExoPlayer `TrackSelection` üzerinden sunucunun orijinal HLS/DASH akışındaki gerçek çoklu kalite varyantları listelendi.

---

### 4. İletişim Bilgileri & Lisans Aktivasyonu
* **Kullanıcı İstekleri:**
  * Premium aktivasyon ve tüm ekranlardan telefon numarasının tamamen kaldırılması.
  * İletişimin yalnızca `geceiptv@gmail.com` e-posta adresi üzerinden sağlanması.
* **Teknik Çözüm ve Kararlar:**
  * `DeviceLicenseScreen.kt` ve strings kaynaklarındaki telefon numaraları temizlendi.
  * Tek resmi destek kanalı olarak `geceiptv@gmail.com` entegre edildi.

---

### 5. TV Ekran Koruyucu & Bekleme Modu Engellemesi (v0.12.02)
* **Kullanıcı İstekleri:**
  * TV'de yayın açıkken birkaç dakika sonra televizyonun ekran koruyucuya veya uyku moduna geçmesi sorununun düzeltilmesi.
* **Teknik Çözüm ve Kararlar:**
  * Oynatıcı ve TV ana görünümlerine `FLAG_KEEP_SCREEN_ON` ve Compose `DisposableEffect` yaşam döngüsüyle ekran açık tutma bayrağı eklendi.
  * Versiyon `v0.12.02` olarak GitHub'a yüklendi.

---

### 6. Büyük Güncelleme Paketi (v0.12.03)
* **Kullanıcı İstekleri:**
  1. Sol hareketli menünün kaldırılması, sabit ana ekran yapılması, altına canlı TV, film, dizi ve QR bağlantı butonları konulması.
  2. **Aktif Sağlayıcı İzolasyonu:** Seçili sağlayıcı dışındaki sağlayıcıların içeriklerinin gizlenmesi; yalnızca aktif olanın verilerinin gösterilmesi.
  3. Telefondaki sağlayıcıların TV ile eşleştiğinde otomatik olarak TV'ye senkronize edilmesi.
  4. Canlı TV'de ülke bazlı gruplandırma ve kanal üzerinde OK basılı tutunca gizleme seçeneği.
  5. Gece/Gündüz tema butonunun saat ve tarihin yanına taşınması.
  6. Kumanda ekranına TV kısayol butonları ve tek tıkla TV'ye medya gönderme (Cast).
  7. Dizi/Film yükleme ekranında yüzdelik (%) ve adet bilgisi sunan hızlandırılmış adım adım yükleme mekanizması.
* **Teknik Çözüm ve Kararlar:**
  * `DashboardScreen.kt`, `OpenTvViewModels.kt` ve `SyncWorker.kt` modüler hale getirildi.
  * Versiyon `v0.12.03` (versionCode 24) olarak derlenip GitHub Release'e yüklendi.

---

### 7. Gemini Modelleri Hakkında Bilgilendirme
* **Kullanıcı Sorusu:** Gemini 4 Argon nasıl kullanılır ve ücretli mi?
* **Açıklama:**
  * Google'ın model isimlendirmeleri (Gemini 1.5 Pro, Flash vb.) ve experimental kod adları hakkında bilgi verildi.
  * Google AI Studio / API katmanındaki ücretsiz ve kullandıkça öde (pay-as-you-go) fiyatlandırma prensipleri açıklandı.

---

### 8. 3 Seçenekli Kanal/Grup Gizleme Modali
* **Kullanıcı İstekleri:**
  * Kanal grubunu hem Canlı TV'de hem de Film ve Dizi sayfasında gruba basılı tutunca direkt gizlemek yerine 3 seçenekli ("Aç", "Gizle", "İptal") bir açılır pencere ile sunulması.
* **Teknik Çözüm ve Kararlar:**
  * `VodScreens.kt` içinde `VodActionModal` composable'ı geliştirildi.
  * Kategori çipleri ve grup listelerine `combinedClickable(onLongClick = { ... })` eklendi.

---

### 9. TV Ana Sayfa Buton Hiyerarşisi
* **Kullanıcı İstekleri:**
  * Arama, Ayarlar ve Çoklu TV butonlarının üstteki Canlı TV / Film / Dizi ızgarasının hemen altına taşınması.
* **Teknik Çözüm ve Kararlar:**
  * `DashboardScreen.kt` içinde TV düzeninde buton çubuğu (`Row`) Canlı TV, Film ve Dizi kartlarının hemen altına alındı.

---

### 10. Telefon Arayüzü Kapsamlı Mobil Optimizasyonu & v0.12.04
* **Kullanıcı İstekleri:**
  1. Film ve dizi sayfalarında sol 260dp rayın telefonlarda alanı kaplamasını önleme; pratik ve modern hale getirme.
  2. Film detay sayfasında kapak fotoğrafının yanındaki "TV'ye Gönder" butonunun dikey ezilip uzamasını düzeltme.
  3. Ayarlar ekranlarındaki (Cihaz ve Lisans, Sağlayıcılar, Kanallar, Görüntü ve Oynatma, Telefon Kumandası) metin ve kartların telefona uyarlanması.
  4. Projenin bir üst versiyon olarak GitHub'a yüklenmesi.
* **Teknik Çözüm ve Kararlar:**
  * **Film & Dizi Mobil:** Sol ray gizlendi; üst yatay kaydırılabilir kategori çipleri (`LazyRow`), kategori arama butonu ve tam ekran 3 sütunlu (`GridCells.Fixed(3)`) Netflix tarzı poster ızgarası uygulandı.
  * **Film Detay & Butonlar:** Kapak görseli orantılı boyutlandırıldı (`100.dp x 150.dp`); butonlar kapak altına yatay kaydırılabilir sıraya (`LazyRow`) alındı ve metinlerin tek satır kalması sağlandı (`maxLines = 1`, `TextOverflow.Ellipsis`).
  * **Ayarlar Hub:** Telefonlarda 2 sütunlu kartlar ve 2 satıra sarılabilen tam başlıklar.
  * **Cihaz ve Lisans:** Tek sütunlu, dikey kaydırmalı (`verticalScroll`) kartlar.
  * **Sağlayıcılar:** İsim/URL üstte, eylem butonları altta iki kademeli kart yapısı.
  * **Kanallar:** Üst yatay kategori çipleri ve %100 genişlikte kanal listesi.
  * **Görüntü ve Oynatma:** Donanım kartı ve optimizasyon diyalogunda dikey kaydırma ile tek sütunlu özellik sıralaması.
  * **Telefon Kumandası:** Ortalanmış, tek sütunlu 180dp QR kod ve okunabilir rehber kartları.
  * **Sürüm Yayınlama:** `app/build.gradle.kts` içinde `versionCode = 25`, `versionName = "0.12.04"` yapıldı. Release APK derlendi, commitlendi, `v0.12.04` tag'i ile GitHub Release'e yüklendi.

### 11. Film & Dizi YouTube Tarzı Akıllı Tamponlama (Forward Buffering + LRU Cache) & TV Arka Plan Ses Düzeltmesi (v0.12.05)
* **Kullanıcı İstekleri:**
  1. Dizi ve film izlerken donma/takılmanın önüne geçmek için YouTube mantığında video açıldığında arka planda ileriye doğru indirme yapan akıllı tampon (forward buffering) eklenmesi; ancak bunun TV ve telefonlarda veri şişkinliği ve hafıza dolmasına sebep olmaması.
  2. TV'de uygulamadan çıkıp başka uygulamaya (Netflix, YouTube vb.) geçildiğinde arka planda birkaç dakika çalışmaya ve ses gelmeye devam etmesi sorununun düzeltilmesi.
* **Teknik Çözüm ve Kararlar:**
  * **VOD İleri Tampon:** `PlayerController.kt` içine `isVod` bayrağı eklendi. VOD için `DefaultLoadControl` min 30s, maks 90s (ayarlardan 120s seçilebilir) ve 30s back-buffer ile yapılandırıldı. Video hızlı (1.5s) başlar ve arka planda 1.5 - 2 dakika ilerisini tamponlar.
  * **LRU Disk Önbellek Kalkanı (`VodCacheManager.kt`):** `SimpleCache` ve `LeastRecentlyUsedCacheEvictor` ile `cacheDir/vod_media_cache` altında **250 MB** kesin sınır konuldu. Eski parçalar otomatik silinir; depolama ve RAM asla şişmez.
  * **İlerleme Çubuğu Görseli:** `VodPlayerScreen.kt` içinde YouTube tarzı açık gri tampon çubuğu eklendi, kullanıcı tampon miktarını görsel olarak izleyebilir.
  * **Kullanıcı Ayarı:** `AppSettings.kt` ve `AppSettingsScreen.kt` içine Film/Dizi akıllı tampon seviyesi (Hızlı 30s, Dengeli 90s, Süper Kararlı 120s) eklendi.
  * **Arka Plan Ses Kesme:** `MainActivity.kt` (`onStop`), `PlayerScreen.kt`, `VodPlayerScreen.kt` ve `MultiViewScreen.kt` içine `LifecycleEventObserver` ve merkezi `PlayerController.pauseAll()` eklendi. TV cihazlarında arka planda ses çalınması ve gereksiz PiP denenmesi engellendi.
  * **Sürüm Yayınlama:** `app/build.gradle.kts` içinde `versionCode = 26`, `versionName = "0.12.05"` yapıldı. Release APK derlendi, commitlendi, `v0.12.05` tag'i ile GitHub Release'e yüklendi.

### 12. Mükerrer Eşitleme Koruması, Tek Tıkla Sağlayıcı Silme, Saatlik Hız Testi & Bağlantı Uyarı Kartı (v0.12.06)
* **Kullanıcı İstekleri:**
  1. Telefondan kumanda ile "Sağlayıcıları Eşitle" butonuna basıldığında TV'de aynı sağlayıcının mükerrer (çift/üçlü) eklenmesinin engellenmesi.
  2. TV'de sağlayıcı silmek istendiğinde çıkan ikinci onay penceresinin kaldırılması; "Kaldır" butonuna basınca sağlayıcının doğrudan silinmesi (kumanda ile ikinci onaya ulaşmak zor oluyordu).
  3. TV uygulamasında seçili sağlayıcının hız testinin saat başı otomatik yapılması ve aktif sağlayıcılar kısmında hız sonucunun yazması.
  4. Sağlayıcının bağlantısı koptuğunda (veya şifre/kullanıcı adı hatalı olduğunda) aktif sağlayıcı renginin maviden kırmızıya dönmesi.
  5. Uygulamanın sağ alt köşesinde *"Lütfen sağlayıcı değiştiriniz. Sağlayıcı bağlantınız yok yada kullanıcı adı şifre yanlış"* detaylı bilgilendirme kartının gösterilmesi.
* **Teknik Çözüm ve Kararlar:**
  * **Mükerrer Sağlayıcı Koruması:** `SourceRepository.kt` içine `isSameSource` ve `findMatchingSource` eklendi; `save()` var olan sağlayıcıyı güncellemeye yönlendirildi. `ManagerServer.kt` içine `alreadyExisted` eklendi; `TvCastHelper.kt` ekranda net Türkçe bildirim gösterir. `CatalogRepository.kt` ve `OpenTvApp.kt` açılışta eski mükerrer kopyaları temizler (`cleanupDuplicateSources()`).
  * **Doğrudan Silme:** `ProvidersScreen.kt` içindeki `confirming` durumu kaldırıldı; "Kaldır" butonu doğrudan `viewModel.delete(source)` çalıştırır.
  * **Saatlik Hız & Sağlık Testi:** `OpenTvViewModels.kt` içine `ProviderHealth` durumu, ping, auth doğrulama ve anlık indirme hızı ölçümü eklendi. `combine` ve `collectLatest` ile her 1 saatte bir (veya aktif sağlayıcı değiştiğinde) otomatik test edilir.
  * **Görsel Durum & Kırmızı Renk:** `DashboardScreen.kt` içinde `ProviderAccordionBar` aktif sağlayıcı yanında `⚡ X Mbps • Y ms` rozeti gösterir; bağlantı koptuğunda bar ve rozetler mavi yerine canlı kırmızıya (`#EF4444`) döner.
  * **Sağ Alt Köşe Uyarısı:** `DashboardScreen.kt` içinde ekranın sağ alt köşesine animasyonlu kırmızı uyarı kartı eklendi; kullanıcıya sağlayıcı değiştirme çağrısı ve hata detayı sunulur.
  * **Sürüm Yayınlama:** `app/build.gradle.kts` içinde `versionCode = 27`, `versionName = "0.12.06"` yapıldı. Release APK derlendi, commitlendi, `v0.12.06` tag'i ile GitHub Release'e yüklendi.

### 13. Akıllı VOD Önbellek Kalkanı, SyncWorker Sonsuz Döngü Engellemesi & TV Performans Optimizasyonu (v0.12.07)
* **Kullanıcı İstekleri:**
  * TV ekranının en altında sürekli "Filmler yükleniyor", "Diziler yükleniyor", "Kanallar yükleniyor" mesajlarının döngü halinde tekrar etmesi sorununun tespiti ve akıllı önbellek kalkanı entegre edilerek loop ve kasma/donmaların engellenmesi.
* **Teknik Çözüm ve Kararlar:**
  * **SyncWorker Sonsuz Retry Loop Engellemesi (`SyncWorker.kt`):** EPG feed'lerindeki harici link hataları (`summary.feedsFailed > 0`) fatal hata kapsamından çıkarıldı ve WorkManager'ın `Result.retry()` döngüsü kırılarak `Result.success()` dönmesi sağlandı. Yalnızca tüm sağlayıcılar tamamen çöktüğünde ve `runAttemptCount < 3` ise yeniden deneme izin verildi. `ExistingPeriodicWorkPolicy.UPDATE` ile eski takılmış işler yenilendi.
  * **Repository Katmanında Akıllı VOD Kalkanı (`CatalogRepository.kt`):** `sync()` ve `syncXtreamVod()` metodlarına `forceVod` bayrağı ve `VOD_TTL_MILLIS = 12 saat` kalkanı eklendi. Veritabanında zaten film/dizi varken ve süre dolmamışsa sunucudan 40.000 içerik tekrar indirilmez; CPU ve ağ şişmesi engellenir.
  * **Çoklu Sağlayıcı Zaman Damgası (`AppSettings.kt`):** Her sağlayıcı için ayrı ayrı (`vod_synced_at_$sourceId`) VOD son senkronizasyon zamanı tutuldu.
  * **StatusBus Güvenliği:** `syncXtreamVod` `try-finally` bloğuna alınarak, herhangi bir iptal veya hatada ekranın altında takılı kalan mesajların anında temizlenmesi (`StatusBus.set(null)`) sağlandı.
  * **Sürüm Güncelleme:** `app/build.gradle.kts` içinde `versionCode = 28`, `versionName = "0.12.07"` yapıldı.

---

## 📌 Hızlı Referans & Mimari Kurallar
1. **Ekran Genişliği Sınırı (Mobil Breakpoint):**
   * `LocalConfiguration.current.screenWidthDp < 600` mobil/telefon cihazları temsil eder.
   * `>= 600dp` veya `isTv` televizyon ve büyük ekranları temsil eder (D-pad gezinmesi ve yan raylar bu ekranda aktiftir).
2. **GitHub Bilgileri:**
   * Repo: `https://github.com/bygokhanege-svg/geceiptv.git`
   * Release APK Adı: `GECEiptv-release.apk`
   * E-posta: `geceiptv@gmail.com`
3. **Derleme Komutları:**
   * Kod Doğrulama: `.\gradlew compileDebugKotlin`
   * Release APK Üretimi: `.\gradlew assembleRelease`
   * Çıktı Yolu: `app\build\outputs\apk\release\GECEiptv-release.apk`
