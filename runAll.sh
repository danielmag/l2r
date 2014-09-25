java -jar l2r.jar AdaRank ./experts/ 50
cp -r experts AdaRank3 - 50
java -jar l2r.jar RankBoost ./experts/ 50 10
cp -r experts RankBoost3 - 50 10
java -jar l2r.jar Coordinate_Ascent ./experts/ 10 20
cp -r experts Coordinate_Ascent3 - 10 20