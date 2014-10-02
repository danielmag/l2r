java -jar l2r.jar AdaRank ./experts/ 50
cp -r experts "AdaRankMap - 50"
java -jar l2r.jar RankNet ./experts/ 30 10
cp -r experts "RankNetMap - 30 10"
java -jar l2r.jar RankBoost ./experts/ 50 10
cp -r experts "RankBoostMap - 50 10"
java -jar l2r.jar Coordinate_Ascent ./experts/ 10 20
cp -r experts "Coordinate_AscentMap - 10 20"