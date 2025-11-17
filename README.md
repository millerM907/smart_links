# Smart Links (multi-module)

Монорепозиторий «Умные ссылки».  
Система состоит из трёх микросервисов:

- **edge** – принимает короткую ссылку и параметры запроса, строит «отпечаток» пользователя, кэширует результат и обращается к движку правил.
- **rules** – по набору атрибутов (браузер, устройство, время, язык и т.п.) и DSL-конфигу в YAML решает, на какой URL нужно отправить пользователя.
- **landing** – рендерит HTML-страницы (лендинги) по slug’у, на который переадресовал edge-сервис.

Процесс обработки клиентского запроса:

1. Клиент открывает `http://localhost:8080/s/sale1111` (Edge).
2. Edge собирает `RequestContext` (заголовки, демо-заголовки `X-Demo-*`, query-параметры), строит fingerprint и проверяет кэш.
3. Если кэша нет — Edge обращается к Rules (`POST http://localhost:8081/resolve`) с `slug` и атрибутами.
4. Rules читает DSL в `rules.yaml`, подбирает подходящее правило и возвращает `targetUrl`.
5. Edge делает HTTP-редирект на `targetUrl` (обычно `http://localhost:8082/landing/...`).
6. Landing отдаёт нужный HTML-шаблон.

## Структура репозитория

```text
smart-links/
  pom.xml                  # parent, multi-module
  edge/
    pom.xml
    src/...
    README.md
  rules/
    pom.xml
    src/...
    README.md
  landing/
    pom.xml
    src/...
    README.md