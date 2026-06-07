# voidrp_webgui

**Форк [WebGUI](https://github.com/mc-webgui/webgui) v1.3.0 — встроенный Chromium-браузер в Minecraft-клиенте для проекта VoidRP.**

Мод встраивает настоящий браузер (через [MCEF](https://github.com/CinemaMod/mcef)) прямо в игровой клиент. Сервер отправляет игроку URL — клиент открывает его как полноэкранный интерфейс или прозрачный HUD-оверлей поверх игры. Весь in-game UI VoidRP реализован на Vue 3 и обслуживается бэкендом (`void-rp.ru`) — никаких Bukkit-инвентарей.

---

## Нововведения v1.3.0 (относительно v1.1.0)

### Двунаправленные события page↔game (v1.2.0)

Сервер теперь может посылать именованные события прямо в JS-контекст открытой страницы, и наоборот — страница может слать события на сервер.

**Server → Page:**
```java
// в любом серверном коде
WebviewApi.emitToPage(player, "market:order_filled", "{\"item\":\"iron\",\"amount\":64}");
```
```js
// в браузере
window.addEventListener("webgui:market:order_filled", e => {
    console.log(e.detail); // { item: "iron", amount: 64 }
});
```

**Page → Server:**
```js
// в браузере
window.webgui.postToServer("shop:buy_clicked", JSON.stringify({ itemId: "iron_sword" }));
```
```java
// регистрация хендлера один раз при старте
WebviewApi.registerPageEventHandler("shop:buy_clicked", (player, json) -> {
    // обработать покупку
});
```

### Entity Binding — привязка WebGUI к сущностям (v1.3.0)

Любую сущность можно привязать к URL: игрок делает ПКМ по ней — открывается браузер.

```
/webgui bind entity <selector> <url> [cancelInteraction]
/webgui unbind entity <selector>
```

В URL поддерживаются плейсхолдеры: `{entityId}`, `{entityType}`, `{playerName}`, `{playerUuid}`.

В JS доступен контекст сущности:
```js
console.log(window.webgui.entity);
// { id: "...", type: "minecraft:villager", x: 100, y: 64, z: 200 }
```

Привязки хранятся в `config/webgui/entity_bindings.json` и переживают перезапуск сервера.

### Горячая перезагрузка конфига

```
/webgui reload   — перечитывает server.json и entity_bindings.json без рестарта
```

---

## Кастомизации этого форка

### NeoForge: фикс channel negotiation disconnect

**Проблема:** На NeoForge + Sinytra Connector оригинальный мод регистрировал S2C-каналы в `WebGUIMod.onInitialize()`. Connector помечал их как «клиент требует от сервера» → NeoForge разрывал соединение при входе с ошибкой `multiplayer.disconnect.incompatible`.

**Фикс:** `WebviewNetworking.registerPayloadTypes()` перенесён исключительно в `WebGUIClient.onInitializeClient()`. Серверная сторона регистрирует только `registerServerReceivers()`.

### Изменённый формат токена

Токен содержит **ник игрока** вместо UUID (оригинал использовал UUID). Причина: Mohist-сервер не хранит Mojang UUID в контексте, доступном из Fabric API.

```
payload = "1|<playerName>|<expiresAtEpoch>"
token   = base64url(payload) + "." + base64url(HMAC-SHA256(payload, secret))
```

Верификация в `WebviewSignedToken.java` соответственно работает с `playerName`, не с UUID.

### Прокси для сборки

В `gradle.properties` добавлены прокси-настройки для загрузки зависимостей через локальный туннель — нужны только при сборке на сервере разработки.

---

## Как используется в VoidRP

### Серверная сторона

Сервер работает на **NeoForge 1.21.1-232** с **Sinytra Connector** — он позволяет запускать Fabric-моды (в том числе этот) на NeoForge. Серверная часть мода работает полноценно: автоматически подписывает токены, открывает HUD при входе, регистрирует команды.

Плагин `voidrp_gamesync_plugin` (Paper) открывает интерфейсы через прямую отправку plugin-channel пакетов (не через команды — Bukkit не диспатчит Fabric-команды через Connector):

```java
// WebGuiBridgeService.java в плагине
webGuiBridge.openGui(player, "https://void-rp.ru/game-ui/market");
webGuiBridge.openHud(player, "https://void-rp.ru/game-ui/hud");
webGuiBridge.sendMainMenuUrl(player, "https://void-rp.ru/game-ui/menu");
```

### Что открывается

| Триггер | Что открывается | Статус |
|---|---|---|
| Вход на сервер | HUD-оверлей | запланировано |
| Клавиша `F6` | Главное меню сервера | запланировано |
| `/pm`, `/shop` | Игровой рынок (книга ордеров, мои ордера, история) | **готово** |
| `/nmarket` | Национальный рынок | запланировано |
| `/ntreasury` | Казна нации | запланировано |
| `/ally` | Альянсы и голосования | запланировано |
| `/bp` | Боевой пропуск | запланировано |
| `/quests` | Ежедневные квесты | запланировано |

### Аутентификация

При каждом открытии URL мод автоматически добавляет подписанный токен (`?webgui_token=...`). Бэкенд верифицирует его через HMAC-SHA256 и определяет игрока по нику — без паролей и сессий.

Секрет хранится в `config/webgui/server.json` на сервере и в `.env` бэкенда (`WEBGUI_TOKEN_SECRET_BASE64`).

---

## План интеграции (статус)

### Этап 0: Фундамент — **готово**

- [x] Сборка и деплой jar (сервер + лаунчер-пак)
- [x] `config/webgui/server.json` настроен (токен, TTL=7200, autoHud)
- [x] Верификация токена в FastAPI (`dependencies/webgui_auth.py`)
- [x] `WebGuiBridgeService.java` в плагине: `openGui`, `openHud`, `sendMainMenuUrl`
- [x] `/webgui reload` — горячая перезагрузка конфига
- [x] Зеркало MCEF на `void-rp.ru/launcher/mcef` (избегает GitHub при старте)
- [x] NeoForge channel negotiation фикс

### Этап 1: Player Market — **готово**

- [x] Бэкенд: `/api/v1/game-ui/market/*` (order book, мои ордера, история, pending actions)
- [x] Плагин: `WebActionPollService` — исполняет buy/cancel/pickup через Vault
- [x] Фронтенд: `GameUiMarketView.vue` (вкладки, polling 5 сек, i18n ru+en)
- [x] `/pm`, `/shop` → `webGuiBridge.openMarket(player)` когда `webgui.enabled: true`

### Этап 2: HUD-оверлей — *в планах*

- [ ] Бэкенд: `GET /game-ui/hud/snapshot` (баланс через кэш, нация, квесты, незабранные доставки)
- [ ] Плагин: пуш баланса в кэш на join и раз в 30 сек
- [ ] Фронтенд: `GameUiHudView.vue` — минималистичный оверлей, polling 10 сек

### Этап 3: Main Menu (F6) — *в планах*

- [ ] Фронтенд: `GameUiMenuView.vue` — кнопки разделов, навигация через `postToGame`

### Этапы 4-8: Nation Market, Treasury, Alliance, Battle Pass, Quests — *в планах*

Паттерн одинаковый для каждого: бэкенд-роутер с `webgui_auth` dependency → pending actions для Vault-операций → Vue view с polling.

### Этап 9: CPM Cosmetics — *в планах*

NeoForge мод открывает WebGUI через Paper-плагин (команда `/vc gui <player>`).

---

## Сборка

```bash
# Сборка под нашу версию (1.21.1)
./gradlew build -P stonecutter.version=1.21.1

# Выходной jar
versions/1.21.1/build/libs/webgui-1.3.0+mc1.21.1.jar
```

Деплой после сборки:
```bash
# Сервер
cp versions/1.21.1/build/libs/webgui-1.3.0+mc1.21.1.jar /home/mironoouv/minecraft/minecraft_server/mods/

# Лаунчер-пак + манифест
cp versions/1.21.1/build/libs/webgui-1.3.0+mc1.21.1.jar /home/mironoouv/launcher/pack/mods/
python3 /home/mironoouv/minecraft/scripts/generate_launcher_manifest.py
```

---

## Конфигурация сервера

Файл `config/webgui/server.json` (генерируется автоматически при первом запуске):

```json
{
  "enableTokens": true,
  "tokenTtlSeconds": 7200,
  "autoHudOnJoin": false,
  "autoHudUrl": "https://void-rp.ru/game-ui/hud",
  "mainMenuUrl": "https://void-rp.ru/game-ui/menu"
}
```

`tokenSecretBase64` генерируется автоматически — скопировать в `.env` бэкенда как `WEBGUI_TOKEN_SECRET_BASE64`.

---

## Серверные команды

```
/webgui gui <игрок> <url>                         — открыть полноэкранный GUI
/webgui hud <игрок> <url>                         — открыть HUD-оверлей
/webgui bind entity <selector> <url> [cancel]     — привязать WebGUI к сущности
/webgui unbind entity <selector>                  — отвязать
/webgui reload                                    — перезагрузить server.json и entity_bindings.json
```

Требует оператора уровня 2.

---

## Структура репозитория

```
src/main/java/land/webgui/
├── api/WebviewApi.java                   — публичный API: emitToPage, registerPageEventHandler
├── server/
│   ├── EntityBinding.java                — модель привязки (url, cancelInteraction)
│   ├── WebviewEntityContext.java         — JSON-контекст сущности для JS
│   ├── WebviewPlaceholders.java          — подстановка {entityId}, {entityType} и др. в URL
│   ├── WebviewServerConfig.java          — читает server.json, хранит секрет
│   ├── WebviewServerEvents.java          — хендлеры page→server событий
│   ├── WebviewSignedToken.java           — HMAC-SHA256 генерация/верификация токенов
│   └── WebviewUrlBuilder.java            — добавляет ?webgui_token= к URL
├── EntityBindingStore.java               — хранит UUID→binding, читает/пишет entity_bindings.json
├── EntityInteractionListener.java        — ПКМ по сущности → открыть привязанный URL
├── WebviewClientEmit.java                — dispatch server→page событий в JS (window.dispatchEvent)
├── WebviewCommands.java                  — /webgui gui|hud|bind|unbind|reload
├── WebviewJoinHud.java                   — авто-HUD и mainMenu при входе
├── WebviewNetworking.java                — S2C пакеты + server receivers
├── WebviewPageToClientBridge.java        — JS→клиент bridge (close, log, run_command и др.)
├── WebviewPayloads.java                  — ID каналов и кодеки всех пакетов
├── WebviewClientBridge.java              — пушит window.webgui.client + entity context на тике
├── WebViewScreen.java                    — полноэкранный GUI экран
└── WebHudOverlay.java                    — прозрачный HUD-оверлей
versions/
├── 1.20.1/                               — конфиг Stonecutter для 1.20.1
├── 1.21.1/                               — наша целевая версия
└── 1.21.11/                              — конфиг Stonecutter для 1.21.11
```

---

## Протокол пакетов

| Канал | Направление | Payload |
|---|---|---|
| `webgui:open_web` | S2C | `VarInt(protocolVersion=1) + VarInt(mode: 0=GUI/1=HUD) + MCString(url)` |
| `webgui:set_main_menu` | S2C | `MCString(url)` |
| `webgui:emit_to_page` | S2C | `MCString(eventName) + MCString(jsonPayload)` |
| `webgui:entity_context` | S2C | `MCString(entityJson)` |
| `webgui:page_to_server` | C2S | `MCString(eventName) + MCString(jsonPayload)` |

Пример прямой отправки из Paper-плагина (если нужно обойти Fabric-команды):
```java
player.sendPluginMessage(plugin, "webgui:open_web", bytes);
```

---

## Лицензия

[MIT](LICENSE) © KoSHeroff (оригинал) — форк для проекта VoidRP.
