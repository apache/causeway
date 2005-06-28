
# This script continously renames the $input to $input.$counter,
# sleeps for one second, increments $counter and starts over until
# $counter reaches $MAX_COUNT.

# The script is used for testing the ResilientFileAppender.

$input=$ARGV[0];
$MAX_COUNT=$ARGV[1];


$counter = 0;

while($MAX_COUNT > $counter) {

  if(!(-e $input)) {
     print "File [$input] does not seem to exist. This can happen.\n";
  } 
  elsif(!(-f $input)) {
     print "[$input] does not seem to be  file.\n";
  } 
  elsif ( !(-w $input)) {
    print "File $input does not seem to be writeeble!!!!\n";
  } 
  else {
    print "Renaming $input to $input.$counter\n";
    rename $input, $input.$counter;
  }
       

  $counter++;

  sleep 1;
}
