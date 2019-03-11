# Manga-Grab

## Purpose
This is a simple CLI program to grab manga from online scans.

## Usages
	manga-grab.groovy ([options]) [url] [chapter_padding] [page_padding] [starting_chapter] [ending_chapter] [request_properties]
* __url__ : with _:chapter:_ for chapter index and _:page:_ for page index. Do Not provide HTTPS links. Only HTTP.
* __Chapter padding__ : Digit padding (1, 01, 001, etc.) 1 for 1, 2 for 01 and so on
* __Page padding__ : same logic as chapter padding
* __Starting chapter__ : Which chapter number to start
* __Ending chapter__ : Which chapter number to end
* __Request Properties__ : Each Headers you want to send to the downloading HTTP Request
### Options
* __-h or --help__ : show usages
* __-z or --zip__ : zip each chapter

### Examples
	manga-grab.groovy 'http://www.funmanga.com/uploads/chapters/15549/:chapter:/p_:page:.jpg' 1 5 0 0
	manga-grab.groovy 'http://images.mangafreak.net/mangas/prison_school/prison_school_:tome:/prison_school_:tome:_:page:.jpg' 1 1 1 1 'Cookie:__cfduid=d0a467a3dbccecddbb1dd9a95d24332191549719778;cf_clearance=af4a3c4d175e3ed83973a1923bfc70f96b44ca1e-1549808750-3600-150'

## Call it from anywhere with adding followings lines on your bash_profile

	function manga-grab() {
		groovy /c/console_commands/manga-grab/manga-grab.groovy $@
	}
