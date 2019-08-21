#Credits
Artwork reused:

* https://commons.wikimedia.org/wiki/File:Vincent_Van_Gogh_-_Wheatfield_with_Crows.jpg by Vincent van Gogh 1890
* https://fr.wikipedia.org/wiki/Fichier:Meuble_corbeau.svg by Henry Salomé le 06/12/2006

Create banner image:
```
convert.exe WheatFieldWithCrows.jpg -channel all isis_clut.png -clut WheatFieldWithCrows.png
```

```
convert.exe WheatFieldWithCrows.jpg -separate -normalize -combine isis_clut.png -clut WheatFieldWithCrows.png
```
