1. В RsiSettingsCache поддерживать вспомогательную структуру Map<String timeframe, Set<Long tgId>> для быстрого таргетинга;
2. Если событие может прийти повторно, заводим recentKeys с TTL;
3. Перейти на JsonSerializer для kafka;
4. Настроить общий ErrorHandler для kafka + DLT топик/ретраи;
5. В логах дебага нагрузка, в инфо метаданные.