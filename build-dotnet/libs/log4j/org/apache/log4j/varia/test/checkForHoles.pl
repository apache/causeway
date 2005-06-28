
# This script checks for holes while rolling a log file.

$input=$ARGV[0];
$MAX_COUNT=$ARGV[1];
$LAST_EXPECTED=$ARGV[2];

$counter = 0;
$expected = 0;	

for($i = $MAX_COUNT; $i >= 1; $i--) {
  checkOrder("$input.$i");
}

checkOrder("$input");

if($expected == $LAST_EXPECTED) {
  print "All files as expected.\n";
}
else {
  print "Missing output or unexpected output in $input$i.\n";
  exit 1;
}


sub checkOrder {
  $file = $_[0];
  if(-e "$file") {
    $firstLine = `head -1 $file`;
    chomp $first;
    $lastLine = `tail -1 $file`;

    if($firstLine =~ /MSG $expected/) {
      print "First line of $file in expected order.\n";
    }
    else {
      print "First line of $file not as expected.\n";
      exit 1;
    }

    if($lastLine =~ /MSG (\d*)/) {
      $expected = $1 + 1;
    }
    else {
      print "last line of $file in unexpected format.\n";
      exit 1;
    }    
  } 
  else {
    print "Skipping inexistent file [$file].\n";
  }
}

