# Память проекта: LightningGrim (форк античита Grim)

Состояние на 13.11.2026 (обновлено после сессии сборки и обзора форков). Исходники теперь лежат распакованными в репозитории `kristaling-vkk/AcKSmp`, ветка `claude/cool-darwin-3t2yyd`, папка `LightningGrim-lightning/`; готовый jar — `dist/GroundedGrim-2.3.75-purpur-1.21.11.jar`.

## Что это за проект

- Исходник: архив `LightningGrim-lightning.zip`, совпадает с публичной веткой `lightning` репозитория `Axionize/LightningGrim` (коммит `652f7c3`).
- Это форк GrimAC (`GrimAnticheat/Grim`, ветка `2.0`). Исходно форк был основан на апстриме `abb95b6` (1 окт. 2026).
- Модули: `common` (вся логика), `bukkit` (Paper/Spigot/Folia), `fabric`.
- Собственные доработки LightningGrim: WallHit и EntityPierce (рейтрейсинг Reach сквозь блоки и сущности), InventoryA–G, `PistonTemplate`/`PlayerPistonData`, `BlockHitData`, `HitboxDebugHandler`, своя реализация `LatencyUtils` с интерфейсом `ILatencyUtils`.
- Итоговая база апстрима сейчас — Grim `85d931f` (7 нояб. 2026), плюс отдельно перенесённые более поздние коммиты (см. раунд 1).

## Сборка (проверено)

- Окружение Claude Code «AcKSmp» открывает репозитории Paper/Fabric/Mojang/PlaceholderAPI/Purpur (`repo.helpch.at`, `api.purpurmc.org` и др.).
- Gradle 9.8.2 нужно запускать на **JDK 25** (Fabric Loom 1.19.3), код компилируется toolchain'ом Java 21: `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 ./gradlew :bukkit:shadowJar -Prelease=true`.
- Maven Central иногда отвечает 429 — просто повторить.
- Тесты: `./gradlew :common:test :bukkit:test` — все проходят (common 278, bukkit 26 на момент первой сборки).
- Проверено на Purpur 1.21.11-2568: плагин грузится, бот mineflayer 4.39 заходит, исключений нет. Для 1.21.2+ бот должен слать `tick_end` (иначе TickTimer), а без `player_input` ходьба откатывается — это особенность бота, старый jar ведёт себя так же.

## История изменений (коммиты в рабочем репозитории)

| Коммит | Что |
|---|---|
| `16e7e6e` baseline | Исходный архив (в него уже попали правки InventoryA/B/C/E из раунда 1) |
| `78dacc1` | Раунд 1: исправления обходов |
| `49a4b26` | Слияние Grim 2.3.74 (апстрим `39d7eb1`) |
| `944548b` | Апстрим `39d7eb1..85d931f` (подготовка к GroundedGrim) |
| `24ef0a9` | Слияние GroundedGrim 2.0 |
| `ca29807` | Перенос 18 эвристических проверок из Grom 1.0.1 |
| `4c3e257` | Ужесточения из LegacyGrim (без послаблений) |
| `24054a2` | Наказания: кик на 300 VL (Timer на 750), флаги хранятся 45 минут |
| `f6fc1ee` | Русская причина кика |

### Раунд 1 — найденные и исправленные обходы

1. **InventoryB** (копание с открытым инвентарём) не была ни к чему подключена → теперь `BlockBreakListener`, отмена откатывает блок у клиента.
2. **InventoryC** не реализовывала `BlockPlaceListener` → события до неё не доходили.
3. **InventoryE**: `lastTransaction = Long.MAX_VALUE` делал флаг невозможным, исходящие пакеты не поступали → `Long.MIN_VALUE` + `PacketSendListener`.
4. **InventoryA** не видела атак новых клиентов (пакет `ATTACK`).
5. **InventoryD** (InvMove): обход через спам USE_ITEM каждый тик → флаг `usedItemWhileOpen`, при котором исключения не действуют.
6. **CrashC**: NaN-ротация в пакете без позиции отравляла симуляцию → pre-via проверка, плюс `VEHICLE_MOVE` (апстрим `1299a2a`).
7. **VehicleA / PacketPlayerSteer**: NaN-ввод транспорта (апстрим `4907ea8`).
8. **BadPacketsQ**: `Math.abs(Integer.MIN_VALUE)` < 0 → явный диапазон.
9. **Boatfly**: шалкер в своей же лодке давал послабление (апстрим `43850df`). То же закрыто в `CompensatedWorld.isNearHardEntity`: там это отключало NoFall и WallHit.
10. Отскок от слизи на ездовых мобах (апстрим `d3fbf34`); известный ввод игрока на серверах ниже 1.21.2 (`3bc8959`); допуск по Y при телепорте только для 1.7; `cancelvl` из config.yml (`1bf0c62`).
11. `CheckManagerListener`: ранний выход для отменённых пакетов движения (до очереди телепортов, Timer и предсказания).

### Слияние Grim 2.3.74

- Поршни отслеживаются по клиентским пакетам block-action (адаптировано под `PistonTemplate` + `CompensatedWorld.addPiston`). Старые `PistonEvent` и миксины поршней удалены.
- Общие секции чанков (copy-on-write), исправления ложных срабатываний, восстановление БД-логов, Folia `teleportAsync`.
- Проверка `teleportId` при принятии телепорта убрана, как в апстриме.
- Версии сборки: Gradle 9.8.2, PacketEvents `2.14.1+addb80e-SNAPSHOT`, Loom `1.18.4-grim.2`.
- В конфликтах выбрана сторона форка: `LatencyUtils`, `BlockHitData` в `BlockPlace`, вызов `Reach.handleBlockChange`.

### Слияние GroundedGrim 2.0

- GroundedGrim ответвился от LightningGrim `5efefe59e` (25 марта 2026); его апстрим — `85d931f`. Слияние трёхстороннее по этой базе.
- Добавилось:
  - проверки AuraSnapBack, BehaviorA/D/E, FreezeAttack, TriggerA, Elytra J–P, AirStuck, BlockFlyGhost, CrashJ, AutoTotem A/B/C, AutoSwapC;
  - EntityPushSimulator, PredictionEngineDualState;
  - анти-ESP / спуф пакетов (выключен по умолчанию), PacketLogger и GrimLogger;
  - конфиг `checks.yml`, тесты.
- Подсистема Reach и рейтрейсинга взята у GroundedGrim целиком (`AttackRay`, `RayTraceShape`, игнорируемые блоки WallHit; список по умолчанию пустой).
- Интерфейса `PacketCheck` больше нет. Проверки реализуют `PacketReceiveListener` / `PacketSendListener` / `BlockPlaceListener` / `PostPredictionListener` и т.д. **Регистрация в `CheckManager` идёт по интерфейсам: без нужного интерфейса проверка молча не получает события.**
- Исправлено: двойная регистрация InventoryA–G в `CheckManager` (падение `ImmutableClassToInstanceMap` при входе игрока).
- `gradle-wrapper.jar` оставлен официальный, апстримный (у GroundedGrim отличался при той же версии).
- `BadPacketsW` удалён (не регистрировался, в апстриме тоже удалён).
- Проверка на бэкдоры пройдена: сеть используется только для проверки релизов на GitHub, захардкоженных исключений нет.
- **Открытый вопрос:** GroundedGrim добавил в InventoryD пропуск `player.wasTouchingWater` (против ложных срабатываний). Это значит, что InvMove в воде не ловится. Оставлено как есть.

### Grom 1.0.1 (18 эвристик)

- Grom основан на апстриме `98fd065dd` (янв. 2026) со старым API, поэтому проверки переписаны, а не слиты:
  - **Aim:** AimConsistent, AimConstant, AimGCD, AimIR, AimRounding, AimSnap;
  - **AutoClicker:** AutoClickerA–G (классы `AutoclickerA`–`G`);
  - **KillAura:** KillAuraZero (`KAZero`), KillAuraCalc (`KACalc`), AuraHitSnap (`AuraPostSnap`; переименована, чтобы не попадать в группу `Post`), AuraMovement;
  - **Scaffold:** ScaffoldDirection, ScaffoldRatio, ScaffoldSync.
- `stableKey` вида `grom.*`. Регистрация стоит после `AimProcessor` (AimGCD и AimSnap читают его результат).
- Новый хелпер `utils/anticheat/AttackPackets` распознаёт атаку и в `INTERACT_ENTITY`, и в `ATTACK`.
- AutoClickerB и ScaffoldSync помечены `experimental` (ложные срабатывания от склейки пакетов сетью).
- Ребрендинг Grom (команды и сообщения) не переносился. Лицензии кода, взятого Grom из других античитов, не проверялись.

### LegacyGrim (только ужесточения — выбор пользователя)

- Перенесено (Bukkit, `platform/bukkit/events`):
  - `BlockPhaseProtection` — фаза через жемчуг и выталкивание из блоков;
  - `EmbeddedInteractionGuard` — запрет действий изнутри полного блока;
  - `BlockPlaceLimiter` — 2 блока за тик;
  - `CrystalSwapGuard` — кристалл в тик свапа, 6 кристаллов в секунду, отмена атаки в тик смены слота;
  - `GlideSwapGuard` — цикл FakeFly.
- Регистрация в `GrimACBukkitLoaderPlugin.registerServerSideGuards()`. Слушатели, которым нужен Paper, пропускаются на Spigot с предупреждением.
- Ключи в config.yml (en, ru): `PearlPhasePrevention`, `PushOutOfBlocks`, `BlockPlaceLimit` (2), `CrystalPlaceLimit` (6), `CrystalSwapPlaceLimit` (0), `AllowCrystalSilentSwap` (false).
- Общий код: WrongBreak (паутина без мгновенного слома не получает исключение); `FINISHED_DIGGING` в креативе не создаёт предсказанный воздух.
- Исправлен NPE в `BlockPlaceLimiter` для неизвестных PacketEvents предметов.
- **Не перенесено (послабления):**
  - анти-отдача, NoSlow, Step, ElytraFly, Strafe, EntitySpeed, Scaffold Tower, InstantRebreak;
  - «пакетные криты» (в LegacyGrim такие пакеты полностью пропускали симуляцию);
  - `onWebFailure` (разрешение «moved wrongly» в паутине);
  - интеграция с FasterCrystals (бинарный jar).

### Наказания (`common/src/main/resources/punishments/*.yml`, 11 языков, 39 групп)

- `remove-violations-after: 2700` (45 минут, значение в секундах) во всех группах; дефолт в `PunishmentManager` тоже 2700.
- Кик на `300:0`, у группы Timer (Timer, TimerLimit, TickTimer и NegativeTimer — по подстроке) на `750:0`.
- Причина кика: `§c§lВы были кикнуты за подозрение в использовании читов`. Используется `§`, а не `&`: Grim отдаёт команду консоли без перевода `&`-кодов. Порядок «цвет, затем жирность» выбран потому, что цвет сбрасывает форматирование.
- Мгновенные баны оставлены: ProtocolBan (1), PlaceBan (1), CrashBan (5), ExploitBan (10).
- Новая группа `Heuristics` (KillAuraZero, AuraHitSnap, AuraMovement, Scaffold*). Aim* попадают в группу `Combat`, AutoClicker* — в `Autoclicker` (сопоставление по подстроке без учёта регистра, `!` исключает).
- Те же значения стоят в шаблонах групп WallHit/EntityPierce, которые плагин дописывает в старые конфиги: `LightningCombatPunishments`, `ConfigManagerFileImpl`. Текст там записан `\u`-экранированием, `%` в `formatted()` — как `%%`.
- На работающем сервере `punishments.yml` не перезаписывается: файл нужно заменить вручную и выполнить `/grim reload`.

### Исправления компиляции после слияния LegacyGrim

Дубли в VectorData (константа WithInput, класс MoveVectorData), GrimPlayer (поля инвентаря, getPossibleLookVectors), PointThreeEstimator (isNearNetherPortal), лишний аргумент Reach.handleBlockChange, лишний @Override в InventoryCheck.

### Обзор всех форков GrimAC (13.11.2026)

- Найдено ~895 репозиториев (поиск GitHub), 452 кандидата скачаны (blob:none), у 211 есть уникальные коммиты в логике проверок (2461 коммит). Отсечены апстрим, наши базы (Axionize/LightningGrim, GroundedGrim) и коммиты, общие для многих форков (ветки PR апстрима).
- **Перенесено (коммит b40bbd3, ветки ManInMyVan — мейнтейнер апстрима, не слиты):** NoSlow (PR #2864, оставлено наше исключение stuck-speed и алерты), KeepAliveOrder → логика в BadPacketsO (имя сохранено), задержка прыжка 10 тиков, InvalidInteractCursor для всех сущностей, `strict-duplicate-handling` (опция, выкл.), inventory-checks: CompensatedOpenWindow, MultiActionsH–S, SprintH, ElytraQ (= его ElytraJ, переименована), `max-clicks-per-tick`. config-version 14 → 15.
- **Перенесено (коммит cc4e6cd):** BetterGrim — сброс использования предмета при флагах BadPacketsH/L; kinchinmatheus — отмена ударов при удержании транзакций (`Reach.max-transaction-delay-ms: 1000`); SolunaMC — ExploitC (таблички), ExploitD (plugin messages), ExploitE (размер/вложенность NBT в кликах), ChatF (спам командами; была ChatE у них), лимиты в CrashB/CrashH, ключи в `exploit:`; ExploitC/D/E добавлены в группу Misc.
- **Отклонено:** RegalManiac «Velocity patch» (на деле опции отключения KB/Explosion — послабление), 6b6t «loose profile» (послабление; persistent-collisions — опция с риском циклов отката), EdGrim ExploitC (грубый лимит 40 блоков/тик, отменённый C03 у нас уже не попадает в симуляцию), BetterGrim ExploitC (бренд на CLIENT_SETTINGS — ложные срабатывания), AutoBlock K0itoyuu (покрыто MultiActionsA/B, Bukkit API не из главного потока), packetorderp (заменён новой PacketOrderP апстрима), Fastechgaming «noslow» (ослабляет флаг до buffer>6), ошибки из aidanvalero уже исправлены у нас.
- **Не разобрано (кандидаты на следующий раунд):** kinchinmatheus (AutoRefill, AutoSoup, ClickRegularity, FastPlace-ужесточения, AuraBot), EdGrim (StuckA, ScaffoldD/E), Anic3 MacroBlockPlace, NothingFlare (AutoTotem, FastBreak, NoFall), MathsAnalysis (AutoArmor, ESP), Fastechgaming (instant swaps/action), skillowiks (TriggerBot), GigaZelensky FastPlace 7/8 (у нас нет этой FastPlace), cyramek/z7087/SaltyAlpha (точность и производительность, не ужесточения).
- **Важно:** большинство проверок помечены experimental и по умолчанию не работают (`experimental-checks: false`): все MultiActions (вкл. новые H–S), PacketOrder, Sprint B–H, ElytraD–Q, BadPacketsC/H/M/P/R/V/X/Z и др. Включение — главный рычаг ужесточения, но с риском ложных срабатываний.

## Открытые задачи и идеи

1. Решить, какие experimental-проверки включить (`enabled-experimental-checks` или `experimental-checks: true`).
2. Решить, нужен ли пропуск InventoryD в воде (обход InvMove против ложных срабатываний).
3. WallHit отключается рядом с лодкой, шалкером или поршнем (задумано, но им можно злоупотребить, поставив лодку рядом).
4. Проверить на живом сервере ложные срабатывания эвристик Grom, прежде чем ужесточать для них наказания.
5. Если нужен бан вместо кика или другие пороги — это правка `punishments/*.yml` (скрипт преобразования сохранял комментарии).

## Файлы-результаты

- `LightningGrim-lightning-fixed.zip` — полный проект в текущем состоянии.
- `merge-grim-2.3.74.patch`, `merge-groundedgrim.patch`, `merge-grom-legacygrim.patch`, `punishments-300-kick.patch` — патчи по шагам, накладываются последовательно через `git apply`.
- `punishments-ru.yml`, `punishments-en.yml` — готовые к замене файлы наказаний.
