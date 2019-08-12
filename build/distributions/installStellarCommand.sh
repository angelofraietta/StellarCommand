# Script to install StellarCommand to a HappyBrackets project

FILENAME=StellarCommand.zip

echo $FILENAME
curl -O http://www.happybrackets.net/downloads/$FILENAME
unzip $FILENAME
rm $FILENAME

cp -a Examples/Device/* ./Device/
cp -a Examples/src/* ./src/
cp -a Examples/libs/* ./libs/
cp -a Examples/.idea/libraries/* ./.idea/libraries/

rm -rf Examples

echo "From IntelliJ File->ProjectStructure"
echo "Click Libraries"
echo "Command Click on StellarCommand Add to Modules"

