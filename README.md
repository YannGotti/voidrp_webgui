# voidrp_webgui

**Форк [WebGUI](https://github.com/mc-webgui/webgui) — встроенный Chromium-браузер в Minecraft-клиенте для проекта VoidRP.**

Мод встраивает настоящий браузер (через [MCEF](https://github.com/CinemaMod/mcef)) прямо в игровой клиент. Сервер отправляет игроку URL — клиент открывает его как полноэкранный интерфейс или прозрачный HUD-оверлей поверх игры. Весь in-game UI VoidRP реализован на Vue 3 и обслуживается бэкендом (`void-rp.ru`) — никаких Bukkit-инвентарей.

---

## Как используется в VoidRP

### Серверная сторона

Сервер работает на **NeoForge 1.21.1-232** с **Sinytra Connector** — он позволяет запускать Fabric-моды (в том числе этот) на NeoForge. Серверная часть мода работает полноценно: автоматически подписывает токены, открывает HUD при входе, регистрирует команды.

Плагин `voidrp_gamesync_plugin` (Paper) открывает интерфейсы через консольную команду:
```
/webgui gui <nick> <url>
/webgui hud <nick> <url>
```

### Что открывается

| Триггер | Что открывается |
|---|---|
| Вход на сервер | HUD-оверлей: баланс, нация, прогресс квестов |
| Клавиша `F6` | Главное меню сервера |
| `/pm`, `/shop` | Игроцкий рынок (книга ордеров, мои ордера) |
| `/nmarket` | Национальный рынок |
| `/ntreasury` | Казна нации |
| `/ally` | Альянсы и голосования |
| `/bp` | Боевой пропуск |
| `/quests` | Ежедневные квесты |

### Аутентификация

При каждом открытии URL мод автоматически добавляет подписанный токен (`?webgui_token=...`). Бэкенд верифицирует его через HMAC-SHA256 и определяет, какой игрок делает запрос — без паролей и сессий.

Секрет хранится в `config/webgui/server.json` на сервере и в `.env` бэкенда.

### Кастомизации этого форка

По сравнению с оригиналом добавлены каналы в `WebviewPageToClientBridge`:

- `{"channel": "run_command", "text": "/pm pickup"}` — выполнить команду от имени игрока прямо со страницы
- `{"channel": "open_gui", "url": "https://..."}` — открыть другой GUI без round-trip через сервер
- `{"channel": "open_hud", "url": "https://..."}` — сменить HUD без round-trip

Это позволяет главному меню (F6) переключать разделы без лишних сетевых запросов.

---

## Клиентская установка

Мод обязателен в модпаке VoidRP и поставляется через лаунчер автоматически. Ручная установка не требуется.

Зависимости (все поставляются лаунчером):
- Fabric API
- MCEF — Chromium (~150 МБ) в комплекте, без авто-скачивания при старте

---

## Сборка

```bash
# Сборка под нашу версию (1.21.1)
./gradlew build -P stonecutter.version=1.21.1

# Выходной jar
versions/1.21.1/build/libs/webgui-*.jar
```

Собранный jar кладётся в клиентский модпак: `/home/mironoouv/launcher/pack/mods/`.

---

## Конфигурация сервера

Файл `config/webgui/server.json` (генерируется автоматически при первом запуске):

```json
{
  "enableTokens": true,
  "tokenTtlSeconds": 7200,
  "autoHudOnJoin": true,
  "autoHudUrl": "https://void-rp.ru/game-ui/hud",
  "mainMenuUrl": "https://void-rp.ru/game-ui/menu"
}
```

`tokenSecretBase64` генерируется автоматически и должен быть скопирован в `.env` бэкенда как `WEBGUI_TOKEN_SECRET_BASE64`.

---

## Серверные команды

```
/webgui gui <игрок> <url>   — открыть полноэкранный GUI
/webgui hud <игрок> <url>   — открыть HUD-оверлей
```

Требует оператора уровня 2 или разрешение `webgui.command` через LuckPerms.

---

## Структура репозитория

```
src/main/java/land/webgui/
├── api/WebviewApi.java               — публичный API для других модов
├── server/
│   ├── WebviewServerConfig.java      — читает server.json, хранит секрет
│   ├── WebviewSignedToken.java       — генерация и верификация HMAC-токенов
│   └── WebviewUrlBuilder.java        — добавляет ?webgui_token= к URL
├── WebviewCommands.java              — /webgui gui|hud
├── WebviewJoinHud.java               — авто-HUD и mainMenu при входе
├── WebviewNetworking.java            — S2C пакеты: webgui:open_web, webgui:set_main_menu
├── WebviewPayloads.java              — ID каналов и кодеки пакетов
├── WebviewPageToClientBridge.java    — JS→клиент bridge (run_command, open_gui и др.)
├── WebviewClientBridge.java          — пушит window.webgui.client на каждом тике
├── WebViewScreen.java                — полноэкранный GUI экран
└── WebHudOverlay.java                — прозрачный HUD-оверлей
versions/
├── 1.20.1/                           — конфиг Stonecutter для 1.20.1
└── 1.21.1/                           — наша целевая версия
```

---

## Протокол пакетов (для справки при отладке)

Если `dispatchCommand` через Connector не работает, плагин может слать пакеты напрямую:

| Канал | Payload |
|---|---|
| `webgui:open_web` | `VarInt(protocolVersion=1) + VarInt(mode: 0=GUI/1=HUD) + MCString(url)` |
| `webgui:set_main_menu` | `MCString(url)` |

```java
player.sendPluginMessage(plugin, "webgui:open_web", bytes);
```

---

## Лицензия

[MIT](LICENSE) © KoSHeroff (оригинал) — форк для проекта VoidRP.
