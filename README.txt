Для запуска тестов понадобится скачать дамп ruwiki с сайта https://dumps.wikimedia.org/wikidatawiki/20180501/
(около 7 файлов в формате xml, запакованные в bz2, например, ruwiki-20180501-pages-articles1.xml-p4p311181.bz2,
нужен хотябы 1 файл)
распаковать их в "src\test\resources\wiki"

Удалить файл из "src\test\resources\wiki" с названием "put_wiki_articles_dump_here_and_DELETE_THIS_FILE.txt"

Тест "MappingTest" можно запустить и без дампа Википедии.

Запустить тесты.

