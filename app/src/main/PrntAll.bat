@echo off
setlocal enabledelayedexpansion

set "outputFile=output.html"
echo ^<html^>^<body^> > "%outputFile%"

for /r %%i in (*.java) do (
	echo ^<h2^>%filename%^</h2^> >> "%outputFile%"
	echo ^<h2^>%%~ni.java^</h2^> >> "%outputFile%"
    echo ^<pre^> >> "%outputFile%"
    for /f "tokens=* delims=" %%a in ('type "%%i"') do (
        set "line=%%a"
        REM set "line=!line:<=^<!"
        REM set "line=!line:>=^>!"
		
		REM החלפת אמפרסנד (&)
        SET "line=!line:&=^&amp;!"
        REM החלפת סימן קטן מ (<)
        SET "line=!line:<=&lt;!"
        REM החלפת סימן גדול מ (>)
        SET "line=!line:>=&gt;!"
        REM החלפת מרכאות כפולות (")
        SET "line=!line:\"=^&quot;!"
        REM החלפת מרכאה בודדת (')
        SET "line=!line:'=^&apos;!"
		

        echo !line! >> "%outputFile%"
    )
    echo ^</pre^> >> "%outputFile%"
echo ^</br^> >> "%outputFile%"
echo ^</br^> >> "%outputFile%"
echo ^</br^> >> "%outputFile%"
)

for /r %%i in (*.xml) do (
	echo ^<h2^>%filename%^</h2^> >> "%outputFile%"
	echo ^<h2^>%%~ni.xml^</h2^> >> "%outputFile%"
    echo ^<pre^> >> "%outputFile%"
	for /f "tokens=* delims=" %%a in ('type "%%i"') do (
        set "line=%%a"
        REM set "line=!line:<=^<!"
        REM set "line=!line:>=^>!"
		
		REM החלפת אמפרסנד (&)
        SET "line=!line:&=^&amp;!"
        REM החלפת סימן קטן מ (<)
        SET "line=!line:<=&lt;!"
        REM החלפת סימן גדול מ (>)
        SET "line=!line:>=&gt;!"
        REM החלפת מרכאות כפולות (")
        SET "line=!line:\"=^&quot;!"
        REM החלפת מרכאה בודדת (')
        SET "line=!line:'=^&apos;!"
		
		
        echo !line! >> "%outputFile%"
    )
    	
    echo ^</pre^> >> "%outputFile%"
	echo ^</br^> >> "%outputFile%"
echo ^</br^> >> "%outputFile%"
echo ^</br^> >> "%outputFile%"

)



echo ^</body^>^</html^> >> "%outputFile%"

echo "הסקריפט סיים. קובץ ה-HTML נוצר: %outputFile%"
endlocal