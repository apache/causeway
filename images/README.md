Create banner image:
```
convert.exe WheatFieldWithCrows.jpg -channel all isis_clut.png -clut WheatFieldWithCrows.png
```

```
convert.exe WheatFieldWithCrows.jpg -separate -normalize -combine isis_clut.png -clut WheatFieldWithCrows.png
```
