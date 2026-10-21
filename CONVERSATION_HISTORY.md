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

### 14. Sağlayıcı Buton Sıralaması, Arama Harf Büyütme/Kapsam Filtresi, EPG Temizliği & Akıllı Kumanda Ergonomisi (v0.12.08)
* **Kullanıcı İstekleri:**
  1. **Sağlayıcı Buton Sıralaması:** Ayarlar > Sağlayıcılar ekranında Hız Testi, Düzenle, Kaldır butonları ile Otomatik, HLS (.m3u8), MPEG-TS (.ts) butonlarının yer değiştirmesi (sık kullanılan format butonları daha kolay erişilebilir olsun).
  2. **Arama Ekranı İyileştirmeleri:** Ekran klavyesine küçük/büyük harf (`a/A`) butonu eklenmesi; arama yapılacak yerin kullanıcı tarafından seçilebilmesi (Tümü, Canlı TV, Filmler, Diziler kapsam filtreleri).
  3. **EPG Rehber Otomatik Temizliği:** Bir sağlayıcı kaldırıldığında TV rehberi (EPG) bölümünde kalan sağlayıcıya ait feed'lerin ve eşleşmelerin otomatik silinmesi, döngü/bug risklerinin önlenmesi.
  4. **Kumanda Otomatik Eşitlemeyi Kaldırma:** Telefonla TV eşleştiğinde otomatik sağlayıcı eşitlemenin kaldırılması; eşitlemenin yalnızca "Sağlayıcıları Eşitle" butonuna basıldığında manuel yapılması (bağlanma anındaki yavaşlığı ve takılmayı önler).
  5. **Yerel Ağda Otomatik Bağlanma:** Bir kez eşleşen telefon ve TV, iki tarafta da uygulama açıksa aynı ağ (Wi-Fi) üzerinden otomatik olarak bağlansın; sürekli QR okutma veya IP girme zorunluluğu kalksın.
  6. **Ergonomik Kumanda Düzeni:** Telefondaki kumandanın joystick yuvarlağı (D-pad) daha aşağıda, sağ el baş parmağın rahatça ulaşabileceği yerde olsun; solunda SES tuşları, sağında KANAL tuşları yer alsın; kumanda daha derli toplu ve pratik hale getirilsin.
* **Teknik Çözüm ve Kararlar:**
  * **Sağlayıcı Format Butonları Önceliği (`ProvidersScreen.kt`):** Hem TV hem mobil arayüzde Otomatik, HLS, MPEG-TS format butonları en başa (üst sıraya), Test, Düzenle ve Kaldır butonları ikinci sıraya taşındı.
  * **Arama Ekranı Klavyesi & Kapsamlar (`SearchScreen.kt`, `OnScreenKeyboard.kt`):**
    * Sanal klavyeye `a/A` Caps/Shift butonu eklendi, harflerin dinamik büyük/küçük harf dönüşümü sağlandı.
    * `SearchScope` (`ALL`, `LIVE`, `MOVIES`, `SERIES`) filtre çipleri eklendi; sonuç sayıları gerçek zamanlı filtrelendi.
  * **EPG Beslemelerini Otomatik Kaldırma (`EpgRepository.kt`, `CatalogRepository.kt`):** `deleteSources()` içine `epgRepository.removeFeedsForSource(id)` eklendi; sağlayıcı silinince ona bağlı EPG XML linkleri ve tablodaki yetim veriler temizlenir.
  * **ManagerServer Kalıcılığı & Port Sabitleme (`ManagerServer.kt`, `AppSettings.kt`):**
    * TV server portu `8765` olarak sabitlendi (meşgulse fallback).
    * Rastgele 5 byte token yerine `settings.getOrCreateManagerToken()` ile SharedPreferences'ta kalıcı token üretildi; TV yeniden başlatılsa bile yetki anahtarı korunur.
    * `GET /ping` endpoint'i token kontrolü önüne alındı; LAN cihazlarının TV'yi ve token geçerliliğini doğrulaması sağlandı. `IDLE_TIMEOUT_MINUTES` 24 saate çıkarıldı.
    * `MainActivity.kt`: TV cihazlarında (`isTelevision`) `managerServer.start()` onCreate'de otomatik başlatılır ve onDestroy'da durdurulur.
  * **Otomatik Eşitlemenin Kaldırılması (`RemoteScreen.kt`):** `parseAndConnect()` içindeki otomatik `TvCastHelper.syncSourcesToTv(...)` çağrısı kaldırıldı. Senkronizasyon sadece kumandadaki "Sağlayıcıları Eşitle" çipine basıldığında çalışır.
  * **Yerel Ağ Otomatik Keşif & Canlı Kalma (`RemoteScreen.kt`):**
    * `pingTv(targetUrl, token)` ve `discoverTvOnLan(token)` (subnet 1..254 paralel tarama) eklendi.
    * `LaunchedEffect`: TV IP'si DHCP ile değişse bile saniyeler içinde otomatik keşfedip bağlanır; 15 saniyelik kalp atışıyla TV'nin açık/kapalı durumu canlı takip edilir.
  * **Sağ Başparmak Odaklı Ergonomik Kumanda Tasarımı (`RemoteScreen.kt`):**
    * **Üst Bar:** Kompakt TV kısayolları (`Canlı TV`, `Film`, `Dizi`, `Anasayfa`, `Sağlayıcıları Eşitle`).
    * **Hızlı Menü Barı:** `Geri`, `Oynat/Duraklat`, `Sessiz`, `Rehber`, `Sayılar (123)`.
    * **Ana Kontrol Bloğu (Thumb Zone):**
      * **Solunda:** `SES` Rocker Kapsülü (Ses Aç, SES etiketi, Ses Kıs) - 62.dp genişlik.
      * **Ortasında:** `Joystick Yuvarlağı` (208.dp çapında D-Pad çemberi; Yukarı, Aşağı, Sol, Sağ ve ortada 74.dp `OK` butonu - sağ başparmağın doğal basma hizasında).
      * **Sağında:** `KANAL` Rocker Kapsülü (Kanal İleri, KANAL etiketi, Kanal Geri) - 62.dp genişlik.
    * **Genişletilebilir Numaratör:** İhtiyaç halinde `Sayılar` butonuna basıldığında altta açılan 0-9 tuş takımı.

### 15. Sıfır Gecikmeli Sanal Fare (Touchpad) Modu & Beliren TV Arama Klavyesi (v0.12.08)
* **Kullanıcı İstekleri:**
  1. Kumanda ekranına bir "Fare" butonu eklenmesi; tıklandığında telefonun TV için kasmadan, donmadan akıcı bir dokunmatik fare (touchpad) olarak kullanılabilmesi.
  2. Fare TV'deki arama butonuna götürülüp tıklandığında (veya TV'de arama ekranı aktif olduğunda) telefondaki mouse alanının hemen altında bir klavye belirmesi; kullanıcının TV'de harf harf seçmek yerine telefondan hızlıca yazıp aratabilmesi ve bu işlemin kasmaya/donmaya yol açmaması.
* **Teknik Çözüm ve Kararlar:**
  * **Sıfır Gecikmeli UDP İletişim Protokolü (`VirtualMouseServer.kt`, Port 8766):**
    * Parmak kaydırmaları saniyede 60-120 olay üretir. HTTP istekleri ağ kuyruğunu tıkayıp TV'de donmaya sebep olacağı için 9 baytlık UDP datagram paketi (`[type: Byte, dx: Float, dy: Float]`) geliştirildi.
    * `Channel<Pair<Float, Float>>(capacity = Channel.CONFLATED)` boru hattı kullanılarak ağ gecikmesi ve bellek yükü 0 ms seviyesine indirildi.
  * **Donanım Hızlandırmalı GPU İmleci (`VirtualMouseOverlay.kt`):**
    * Standart Compose recomposition yerine `graphicsLayer { translationX = pointerX; translationY = pointerY }` ile GPU katmanında 60/120 FPS akıcı imleç hareketi sağlandı; ekrandaki diğer hiçbir bileşen gereksiz yere yeniden çizilmez.
    * Keskin ok imleci, gölge kenarlığı ve tıklama anında yayılan nabız animasyonu (`isClicking`) eklendi. 12 saniye hareketsizlik sonrası otomatik gizlenme uygulandı.
  * **Ekrana Dokunma Enjeksiyonu (`window.decorView.dispatchTouchEvent`):**
    * Fare tıklandığında imlecin altındaki koordinata sentetik `ACTION_DOWN` ve `ACTION_UP` dokunma olayları gönderilir; TV arayüzündeki butonlar, kartlar ve arama çubuğu doğrudan tetiklenir.
  * **Beliren Klavye & Arama Entegrasyonu (`RemoteScreen.kt`, `MainActivity.kt`):**
    * TV'de arama ekranına girildiğinde (`Routes.search`) `NavController.OnDestinationChangedListener` tetiklenir ve `ManagerServer.isSearchActive = true` bayrağı `/ping` yanıtında telefona iletilir.
    * Fare modundayken Touchpad ve Sol/Sağ tık butonlarının hemen altında yer alan klavye kartı turkuaz ışıkla (`TV ARAMA EKRANI AKTİF — Buradan Hızlıca Yazın`) parlar; `focusRequester` ve `keyboardController` ile telefon klavyesi otomatik olarak açılır.
    * Kullanıcı telefondan metni yazıp "TV'de Ara & Gönder" veya klavyeden Ara/Enter tuşuna bastığında sorgu 0 ms gecikmeyle doğrudan TV'ye aktarılır.
  * **Kumanda & Fare Geçiş Ergonomisi (`RemoteScreen.kt`):**
    * Hızlı menü barına `Fare` butonu (açık/kapalı ışıklı durum göstergesiyle) entegre edildi.
    * Fare moduna geçildiğinde üstte `[ 🎮 Kumandaya Dön ]` butonu, ortada 210.dp hassas Touchpad yüzeyi, altında geniş `SOL TIK (SEÇ)` ve `SAĞ TIK (GERİ)` butonları ve hemen altında Hızlı TV Arama Klavyesi yer alır.

### 16. Sanal Fare (Touchpad) & TV Klavyesi Entegrasyonu, Tam Çoklu Dil (TR & EN) Desteği & v0.12.09
* **Kullanıcı İstekleri:**
  1. Kumanda ekranına bir fare (touchpad) butonu eklenmesi; telefon ekranının kasmadan, donmadan TV'de fare gibi kullanılabilmesi.
  2. TV arama ekranına girildiğinde telefondaki farenin altında anında klavye belirmesi ve TV'ye doğrudan hızlı arama metni gönderilebilmesi.
  3. Uygulamanın tamamının detaylı dil analizinden geçirilmesi; sonradan eklenen ve sert kodlanmış (hardcoded) tüm buton, etiket, açıklama, diyalog, tost ve panellerin eksiksiz İngilizce ve Türkçe kaynaklara (`strings.xml`) bağlanarak çoklu dil desteğinin tamamlanması.
  4. Kod doğrulamasının yapılması, sürümün güncellenmesi ve GitHub'a release olarak yüklenmesi.
* **Teknik Çözüm ve Kararlar:**
  * **Sanal Fare & UDP Protokolü (`VirtualMouseServer.kt`, `VirtualMouseOverlay.kt`, `RemoteScreen.kt`):** Port 8766 üzerinde sıfır gecikmeli UDP paketleri, GPU donanım hızlandırmalı imleç overlay'i ve `dispatchTouchEvent` sentetik dokunma tetikleyicisi.
  * **Beliren Akıllı Klavye:** TV'de arama ekranı açıldığında telefon klavyesinin otomatik belirmesi ve doğrudan TV arama motoruna iletilmesi.
  * **Eksiksiz Dil Yerelleştirmesi (`values/strings.xml`, `values-tr/strings.xml`):**
    * Kumanda & Fare (`remote_*`): Touchpad modu, D-pad, ses/kanal düğmeleri, arama kartı, QR/IP diyalogları.
    * Sağlayıcı Hız Testi & Teşhis (`provider_*`): Ping, hız (Mbps), hesap bitiş/oluşturulma tarihleri, yayın formatları.
    * Web Yönetici & Eşleşme (`web_manager_*`): Canlı bağlantı rozetleri, rehber adımları, güvenlik notları.
    * VOD Oynatıcı, Altyazı & Akıllı Senkron (`vod_*`): Harici oynatıcı (VLC/MX), sonraki bölüm kartları, OpenSubtitles/TheSubtitleDB çoklu arama, milisaniye hassasiyetli akıllı senkron HUD'ı, sıralama seçenekleri.
    * Kategori ve Kanal Eylemleri (`category_action_*`, `channel_action_*`): Aç, gizle, iptal, toast bildirimleri.
  * **Sürüm Yayınlama:** `app/build.gradle.kts` içinde `versionCode = 30`, `versionName = "0.12.09"` yapıldı.

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
