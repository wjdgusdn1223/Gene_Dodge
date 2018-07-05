package myPackage;

import java.io.*;
import java.util.Arrays;

class bombGame{
	/*
	 * 칸은 우선 x * x로 x*x칸을 만든다.
	 * 너무 넓으면 운에 의한 장기 플레이가 예상되므로 안됨
	 * 너무 좁으면 폭탄이 터지는 반경에 의해 난이도가 매우 상승함
	 * 
	 * 문제발견 - 폭탄 1개는 너무 안일했다고 생각됨 
	 * 			생각보다 컴퓨터의 성능이 너무 좋음 
	 * 			고로 폭탄 2개로 설정해 보겠슴.
	 * 
	 * 매 턴마다 폭탄 위치가 설정되고 다음 턴에 바로 터진다.
	 * 폭탄 위치는 랜덤으로 설정된다.
	 * 폭탄이 터지는 반경은 사방 1칸 까지가 폭발 범위임('+'모양임)
	 * 플레이어는 최초 자신이 있는 칸과 주위의 칸을 포함 9칸에 대한 경우의 수
	 * 에 대한 행동 규칙을 숫자 9자리로 받는다(랜덤으로).
	 * 
	 * 문제발견 - 폭탄이 + 모양으로 터지기 때문에 9칸의 범위에 없을 때
	 * 			랜덤으로 움직이게 되면 폭발 반경으로 들어갈 경우가 생김
	 * 			추가로 센서 2개 (가로 세로 센서)가 필요할 것 같음
	 * 문제 1차 해결 - 가로 세로 센서 추가 완료
	 * 
	 * 문제발견 - 폭탄이 두개이기 때문에 두개가 동시에 감지될 경우
	 * 문제 2차 해결 - 센서 추가 완료
	 *
	 * 
	 * 총  x턴을 한뒤 폭탄에 몇번이나 맞았는지. 계산한 뒤
	 * 선택 - 10개체 중 2개체를 뽑는다. 순위기반 선택 방식으로 뽑는다.
	 * 교배 - 2개체를 교차연산하여 총 12가지의 개체를 만든다.
	 * 
	 * 문제발견 - 교차연산을 생각하던 중 범위로 잘라 교배하는 것 보다는
	 * 			랜덤으로 둘 중 어떤 유전정보를 받을지 선택해서
	 * 			좀더 확실히 두가지의 유전 정보가 섞이게 하는 편이 나을 거라는
	 * 			생각이 듬 
	 * 
	 * 돌연변이 - 순위기반 선택방식에서의 단점을 랜덤개체 2개를 만듦으로서 해결한다.
	 * 해당되는 모든 행동규칙 및 결과는 파일 입출력으로 저장된다.
	 * 파일 입출력의 과정을 설명하겠다.
	 * 
	 * 1.최초 10개체의 유전 정보를 랜덤하게 저장
	 * 2.10개체의 유전정보를 읽어 게임 실행
	 * 3.10 개체의 결과를 파일에 저장한다.
	 * 4.파일을 읽어 해당 세대중 뛰어난 2개의 개체를 읽는다.
	 * 5.교차 및 돌연변이를 이용해 만든 20개체를 파일에 저장한다.
	 * 6.조건을 만족할 때 까지 2 ~ 5를 반복한다.
	 * 7.조건을 만족했다면 마지막 결과를 저장후 프로그램을 저장한다. 
	 * 
	 * 있어야 하는 메서드
	 * 1. 메인으로서 돌아가는 게임을 제어하는 메서드
	 * 2. 폭탄의 위치를 설정하고 폭파시키는 메서드
	 * 3. 플레이어의 센서를 담당하고 해당 값을 리턴시키는 메서드
	 * 4. 폭탄에 맞았는지 아닌지 판단하는 메서드
	 * 5. 랜덤값으로 개체를 생성하는 메서드
	 * 6. 순위기반으로 개체를 선택할 메서드
	 * 7. 교차연산으로 교배를 실행할 메서드
	 * 8. 결과 값 및 개체 유전정보를 파일에 입력시킬 메서드
	 * 9. 메인에게 x개체의 유전정보를 파일로 부터 읽어 리턴 시켜줄 메서드
	 * 10.유전정보대로 실행시킬 메서드
	 */
	//게임판 생성
	int a = 0;
	final int TABLE_SIZE;
	private int[][] gameTable;
	//10개체 유전 정보를 담을 배열
	final int GENE_NUM;
	private String[] geneList;
	private String[] nextGeneList;
	//랜덤 값
	private int random;
	//x번째 세대
	private int numOfGeneration;
	final int NUM_OF_FINAL_GENE;
	//파일을 읽을 때 해당 세대 찾았는지 여부
	private boolean gnFound;
	//10줄만 읽어들이게 하는 변수
	private int limit10;
	//플레이어 최초 좌표
	private int p_X;
	private int p_Y;
	//폭탄의 좌표
	private int b_X_1;
	private int b_Y_1;
	//private int b_X_2;
	//private int b_Y_2;
	//플레이어의 센서 1~9
	private int bombCensor[] = new int[2];
	private int bombDetected;
	//플레이어 움직임 변수
	private int playerGoTo;
	//폭탄에 맞은 횟수
	private int deadNum[];
	//최초 세대와 신 세대 구분
	private boolean firstGeneration = true;
	//교차연산 할때 랜덤으로 섞어주는 변수
	private int crossOverValue;
	//순위기반으로 몇번째 개체인지 판별하는 변수
	private int rankFirst;
	private int rankSecond = 1;
	//생성자의 매개 변수로 게임판의 사이즈와 개체 개수, 결과값 개수를 선택
	public bombGame(int tB, int gN, int fgn){
		TABLE_SIZE = tB;
		GENE_NUM = gN;
		NUM_OF_FINAL_GENE = fgn;
		
		gameTable = new int[TABLE_SIZE][TABLE_SIZE];
		geneList = new String[GENE_NUM];
		nextGeneList = new String[GENE_NUM];
		deadNum = new int[GENE_NUM];
	}
	
	void gameMain(){
		//최초 x개체를 랜덤하게 저장한다.
		for(; numOfGeneration < NUM_OF_FINAL_GENE;numOfGeneration++){
			makeMutation(firstGeneration);
			inputGeneFromFile();
			for(int mainTurn = 0; mainTurn < GENE_NUM; mainTurn++)
				playGame(mainTurn);
			outputGeneToFile();
			firstGeneration = false;
			if(numOfGeneration >= 4)
				if(numOfGeneration % 
					(int)(numOfGeneration/(NUM_OF_FINAL_GENE/100)) == 0)
				System.out.println("로딩 중 " + 
					(numOfGeneration/(NUM_OF_FINAL_GENE/100)) + "% 완료");
		}
		/*System.out.println(Arrays.toString(deadNum));
		System.out.println(a);*/
	}
	
	private void playGame(int turnMain){
		/*
		 * 1.플레이어 좌표 표시
		 * 2.폭탄 좌표 표시
		 * 3.센서 작동
		 * 4.규칙에 따른 행동 개시
		 * 5_1.폭탄 터짐
		 * 5_2.맞은 여부확인
		 *
		 * 2~5까지의 과정 x번 반복
		 */
		p_X = 1;
		p_Y = 1;
		gameTable[p_Y][p_X] = 8;
		for(int tmp = 0; tmp < 10000; tmp++){
			bombControl();
			playerCensor();
			/*for(int iCount = 0; iCount < TABLE_SIZE; iCount++){
				for(int iCount2 = 0; iCount2 < TABLE_SIZE; iCount2++)
					System.out.print(gameTable[iCount][iCount2] + " ");
				System.out.println();
			}
			System.out.println("----------" + tmp + " " + deadNum[turnMain]);*/
			playerControl(turnMain);
			if(deadOrAlive())
				deadNum[turnMain]++;
		}
		
		for(int tmp = 0; tmp < TABLE_SIZE; tmp++)
			for(int tmp2 = 0; tmp2 < TABLE_SIZE; tmp2++)
				gameTable[tmp][tmp2] = 0;
	}
	
	private void playerControl(int mainNum){
		String geneSplit[] = geneList[mainNum].split("");
		
		if(bombCensor[0] == 12 && bombCensor[1] == 12)
			playerGoTo = (int)(Math.random()*5);
		else{
			random = (int)(Math.random()*2);
			if(bombCensor[0] == 12 && bombCensor[1] != 12) 
				playerGoTo = Integer.valueOf(geneSplit[bombCensor[1]]);
			else if(bombCensor[1] == 12 && bombCensor[0] != 12)
				playerGoTo = Integer.valueOf(geneSplit[bombCensor[0]]);
			else{
				playerGoTo = Integer.valueOf(geneSplit[bombCensor[random]]);
			}
		}
		
		if(!(gameTable[p_Y][p_X] == 4))
			gameTable[p_Y][p_X] = 0;
		
		switch(playerGoTo){
		case 0 ://제자리
			break;
		case 1 ://왼쪽
			if(p_X == 0)
				p_X = (TABLE_SIZE-1);
			else
				p_X -= 1;
			break;
		case 2 ://위쪽
			if(p_Y == 0)
				p_Y = (TABLE_SIZE-1);
			else
				p_Y -= 1;
			break;
		case 3 ://오른쪽
			if(p_X == (TABLE_SIZE-1))
				p_X = 0;
			else
				p_X += 1;
			break;
		case 4 ://아래쪽
			if(p_Y == (TABLE_SIZE-1))
				p_Y = 0;
			else
				p_Y += 1;
			break;
		}
		if(!(gameTable[p_Y][p_X] == 4))
			gameTable[p_Y][p_X] = 8;
	}
	
	private void bombControl(){
		if(!(p_X == b_X_1 && p_Y == b_Y_1))
			gameTable[b_Y_1][b_X_1] = 0;
		else
			gameTable[b_Y_1][b_X_1] = 8;
		
		/*if(!(p_X == b_X_2 && p_Y == b_Y_2))
			gameTable[b_Y_2][b_X_2] = 0;
		else
			gameTable[b_Y_2][b_X_2] = 8;*/
		
		do{
			b_X_1 = (int)((Math.random())*TABLE_SIZE);
			b_Y_1 = (int)((Math.random())*TABLE_SIZE);
		}while(p_X == b_X_1 && p_Y == b_Y_1);
		
		/*do{
			b_X_2 = (int)((Math.random())*TABLE_SIZE);
			b_Y_2 = (int)((Math.random())*TABLE_SIZE);
		}while((p_X == b_X_2 && p_Y == b_Y_2) &&
			   (b_X_1 == b_X_2 && b_Y_1 == b_Y_2));*/
		
		gameTable[b_Y_1][b_X_1] = 4;
		//gameTable[b_Y_2][b_X_2] = 4;
	}
	
	private void playerCensor(){
		if(((p_X == 0 && p_Y == 0) && (gameTable[(TABLE_SIZE-1)][(TABLE_SIZE-1)] == 4)) ||
		   ((p_X == 0 && p_Y != 0) && (gameTable[p_Y-1][(TABLE_SIZE-1)] == 4)) ||
		   ((p_X != 0 && p_Y == 0) && (gameTable[(TABLE_SIZE-1)][p_X-1] == 4)) ||
		   ((p_X != 0 && p_Y != 0) && (gameTable[p_Y-1][p_X-1] == 4))){
				bombCensor[0] = 0;
				bombDetected++;
		}
		if(((p_Y == 0) && (gameTable[(TABLE_SIZE-1)][p_X] == 4)) ||
				((p_Y != 0) && (gameTable[p_Y-1][p_X] == 4))){
			if(bombDetected != 1)
				bombCensor[0] = 1;
			else
				bombCensor[1] = 1;
			bombDetected++;
		}
		if(((p_X == (TABLE_SIZE-1) && p_Y == 0) && (gameTable[(TABLE_SIZE-1)][0] == 4)) ||
			    ((p_X == (TABLE_SIZE-1) && p_Y != 0) && (gameTable[p_Y-1][0] == 4)) ||
			    ((p_X != (TABLE_SIZE-1) && p_Y == 0) && (gameTable[(TABLE_SIZE-1)][p_X+1] == 4)) ||
			    ((p_X != (TABLE_SIZE-1) && p_Y != 0) && (gameTable[p_Y-1][p_X+1] == 4))){
			if(bombDetected != 1)
				bombCensor[0] = 2;
			else
				bombCensor[1] = 2;
			bombDetected++;
		}
		if(((p_X == 0) && (gameTable[p_Y][(TABLE_SIZE-1)] == 4)) ||
				((p_X != 0) && (gameTable[p_Y][p_X-1] == 4))){
			if(bombDetected != 1)
				bombCensor[0] = 3;
			else
				bombCensor[1] = 3;
			bombDetected++;
		}
		if(gameTable[p_Y][p_X] == 4){
			if(bombDetected != 1)
				bombCensor[0] = 4;
			else
				bombCensor[1] = 4;
			bombDetected++;
		}
		if(((p_X == (TABLE_SIZE-1)) && (gameTable[p_Y][0] == 4)) ||
				((p_X != (TABLE_SIZE-1)) && (gameTable[p_Y][p_X+1] == 4))){
			if(bombDetected != 1)
				bombCensor[0] = 5;
			else
				bombCensor[1] = 5;
			bombDetected++;
		}
		if(((p_X == 0 && p_Y == (TABLE_SIZE-1)) && (gameTable[0][(TABLE_SIZE-1)] == 4)) ||
			    ((p_X == 0 && p_Y != (TABLE_SIZE-1)) && (gameTable[p_Y+1][(TABLE_SIZE-1)] == 4)) ||
			    ((p_X != 0 && p_Y == (TABLE_SIZE-1)) && (gameTable[0][p_X-1] == 4)) ||
			    ((p_X != 0 && p_Y != (TABLE_SIZE-1)) && (gameTable[p_Y+1][p_X-1] == 4))){
			if(bombDetected != 1)
				bombCensor[0] = 6;
			else
				bombCensor[1] = 6;
			bombDetected++;
		}
		if(((p_Y == (TABLE_SIZE-1)) && (gameTable[0][p_X] == 4)) ||
				((p_Y != (TABLE_SIZE-1)) && (gameTable[p_Y+1][p_X] == 4))){
			if(bombDetected != 1)
				bombCensor[0] = 7;
			else
				bombCensor[1] = 7;
			bombDetected++;
		}
		if(((p_X == (TABLE_SIZE-1) && p_Y == (TABLE_SIZE-1)) && (gameTable[0][0] == 4)) ||
			    ((p_X == (TABLE_SIZE-1) && p_Y != (TABLE_SIZE-1)) && (gameTable[p_Y+1][0] == 4)) ||
			    ((p_X != (TABLE_SIZE-1) && p_Y == (TABLE_SIZE-1)) && (gameTable[0][p_X+1] == 4)) ||
			    ((p_X != (TABLE_SIZE-1) && p_Y != (TABLE_SIZE-1)) && (gameTable[p_Y+1][p_X+1] == 4))){
			if(bombDetected != 1)
				bombCensor[0] = 8;
			else
				bombCensor[1] = 8;
			bombDetected++;
		}
		if(p_X == b_X_1 && p_Y != b_Y_1){
			if(bombDetected != 1)
				bombCensor[0] = 9;
			else
				bombCensor[1] = 9;
			bombDetected++;
		}
		if(p_X != b_X_1 && p_Y == b_Y_1){
			if(bombDetected != 1)
				bombCensor[0] = 10;
			else
				bombCensor[1] = 10;
			bombDetected++;
		}
		if(bombDetected == 1){
			bombCensor[1] = 12;
		}
		else if(bombDetected == 0){
			bombCensor[0] = 12;
		 	bombCensor[1] = 12;
		}
		else if(bombDetected == 2){
			bombCensor[0] = 11;
			bombCensor[0] = 11;
		}
		bombDetected = 0;
	}
	
	private boolean deadOrAlive(){
		//사방 모서리에 있을때 4가지
		//4개 구석에 있을때 4가지
		//맵 안쪽에 있을 때 1가지
		if((b_X_1 == 0 && b_Y_1 != 0 && b_Y_1 != (TABLE_SIZE-1)) &&
		   ((p_X == 0 && p_Y == (b_Y_1-1)) || 
			(p_X == 0 && p_Y ==  b_Y_1   ) ||
			(p_X == 0 && p_Y == (b_Y_1+1)) ||
			(p_X == 1 && p_Y ==  b_Y_1   ) ||
			(p_X == (TABLE_SIZE-1) && p_Y ==  b_Y_1   )))
			return true;
		else if((b_Y_1 == 0 && b_X_1 != 0 && b_X_1 != (TABLE_SIZE-1)) &&
				((p_Y == 0 && p_X == (b_X_1-1)) || 
				 (p_Y == 0 && p_X ==  b_X_1   ) ||
				 (p_Y == 0 && p_X == (b_X_1+1)) ||
				 (p_Y == 1 && p_X ==  b_X_1   ) ||
				 (p_Y == (TABLE_SIZE-1) && p_X ==  b_X_1   )))
			return true;
		else if((b_X_1 == (TABLE_SIZE-1) && b_Y_1 != 0 && b_Y_1 != (TABLE_SIZE-1)) &&
				((p_X == (TABLE_SIZE-1) && p_Y == (b_Y_1-1)) || 
				 (p_X == (TABLE_SIZE-1) && p_Y ==  b_Y_1   ) ||
				 (p_X == (TABLE_SIZE-1) && p_Y == (b_Y_1+1)) ||
				 (p_X == (TABLE_SIZE-2) && p_Y ==  b_Y_1   ) ||
				 (p_X == 0 && p_Y ==  b_Y_1   )))
			return true;
		else if((b_Y_1 == (TABLE_SIZE-1) && b_X_1 != 0 && b_X_1 != (TABLE_SIZE-1)) &&
				((p_Y == (TABLE_SIZE-1) && p_X == (b_X_1-1)) || 
				 (p_Y == (TABLE_SIZE-1) && p_X ==  b_X_1   ) ||
				 (p_Y == (TABLE_SIZE-1) && p_X == (b_X_1+1)) ||
				 (p_Y == (TABLE_SIZE-2) && p_X ==  b_X_1   ) ||
				 (p_Y == 0 && p_X ==  b_X_1   )))
			return true;
		else if((b_X_1 == 0 && b_Y_1 == 0) &&
				((p_X == 0 && p_Y == 0) ||
				 (p_X == 0 && p_Y == 1) ||
				 (p_X == 0 && p_Y == (TABLE_SIZE-1)) ||
				 (p_X == 1 && p_Y == 0) ||
				 (p_X == (TABLE_SIZE-1) && p_Y == 0)))
			return true;
		else if((b_X_1 == (TABLE_SIZE-1) && b_Y_1 == 0) &&
				((p_X == (TABLE_SIZE-1) && p_Y == 0) ||
				 (p_X == (TABLE_SIZE-1) && p_Y == 1) ||
				 (p_X == (TABLE_SIZE-1) && p_Y == (TABLE_SIZE-1)) ||
				 (p_X == 0 && p_Y == 0) ||
				 (p_X == (TABLE_SIZE-2) && p_Y == 0)))
			return true;
		else if((b_X_1 == 0 && b_Y_1 == (TABLE_SIZE-1)) &&
				((p_X == 0 && p_Y == 0) ||
				 (p_X == 0 && p_Y == (TABLE_SIZE-2)) ||
				 (p_X == 0 && p_Y == (TABLE_SIZE-1)) ||
				 (p_X == 1 && p_Y == (TABLE_SIZE-1)) ||
				 (p_X == (TABLE_SIZE-1) && p_Y == (TABLE_SIZE-1))))
			return true;
		else if((b_X_1 == (TABLE_SIZE-1) && b_Y_1 == (TABLE_SIZE-1)) &&
				((p_X == (TABLE_SIZE-1) && p_Y == 0) ||
				 (p_X == (TABLE_SIZE-1) && p_Y == (TABLE_SIZE-2)) ||
				 (p_X == (TABLE_SIZE-1) && p_Y == (TABLE_SIZE-1)) ||
				 (p_X == 0 && p_Y == (TABLE_SIZE-1)) ||
				 (p_X == (TABLE_SIZE-2) && p_Y == (TABLE_SIZE-1))))
			return true;
		else if(((b_X_1 != 0 && b_X_1 != (TABLE_SIZE-1)) &&
				 (b_Y_1 != 0 && b_Y_1 != (TABLE_SIZE-1))) &&
				 ((p_X ==  b_X_1    && p_Y == (b_Y_1-1)) ||
				  (p_X ==  b_X_1    && p_Y ==  b_Y_1   ) ||
				  (p_X ==  b_X_1    && p_Y == (b_Y_1+1)) ||
				  (p_X == (b_X_1-1) && p_Y ==  b_Y_1   ) ||
				  (p_X == (b_X_1+1) && p_Y ==  b_Y_1   )))
			return true;
		return false;
	}
	
	private void makeMutation(boolean firstTime){
		if(firstTime){
			try {
				PrintWriter output = new PrintWriter(
						"C:/Users/정현우/Desktop/WorkSetting/HelloJava/gene.txt");
				output.println("#" + numOfGeneration + "_Generation");
				output.println("----------");
				for(int iCount = 0; iCount < GENE_NUM; iCount++){
					for(int tmp = 0; tmp < 12; tmp ++){
						random = (int) ((Math.random())*4);
						output.print(random);
					}
					output.println();
				}
				output.println("----------");
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else{
			try {
				PrintWriter output = new PrintWriter(new FileWriter(
				"C:/Users/정현우/Desktop/WorkSetting/HelloJava/gene.txt",true));
				
				rankSelect();
				makeCrossOver();
				
				String rank1List[] = geneList[rankFirst].split("");
				String rank2List[] = geneList[rankSecond].split("");
				
				for(int iCount = 12; iCount < GENE_NUM; iCount++){
					for(int tmp = 0; tmp < 12; tmp ++){
						random = (int) (Math.random()*5);
						if(random == 1 || random == 3)
							nextGeneList[iCount] += rank1List[tmp];
						else if(random == 2)
							nextGeneList[iCount] += rank2List[tmp];
						else
							nextGeneList[iCount] += String.valueOf(random);
					}
				}
				
				output.println("#" + numOfGeneration + "_Generation");
				output.println("----------");
				for(int iCount = 0; iCount < GENE_NUM; iCount++){
					output.println(nextGeneList[iCount]);
				}
				output.println("----------");
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void rankSelect(){
		int temp;
		if(deadNum[rankFirst] > deadNum[rankSecond]){
			temp = rankFirst;
			rankFirst = rankSecond;
			rankSecond = temp;
		}
		for(int tmp = 2; tmp < deadNum.length; tmp++){
			if(deadNum[rankSecond] > deadNum[tmp])
				rankSecond = tmp;
			
			if(deadNum[rankFirst] > deadNum[rankSecond]){
				temp = rankFirst;
				rankFirst = rankSecond;
				rankSecond = temp;
			}
		}
	}
	
	private void makeCrossOver(){
		
		for(int tmp = 0; tmp < GENE_NUM; tmp++)
				nextGeneList[tmp] = "";
		
		String rank1List[] = geneList[rankFirst].split("");
		String rank2List[] = geneList[rankSecond].split("");
		for(int tmp = 0; tmp < 12; tmp++){
			for(int iCount = 0; iCount < 12; iCount++){
				crossOverValue = (int)((Math.random()*2)+1);
				if(crossOverValue == 1)
					nextGeneList[tmp] += rank1List[iCount];
				else
					nextGeneList[tmp] += rank2List[iCount];
			}
		}
		
		rankFirst = 0;
		rankSecond = 1;
		for(int tmp = 0; tmp < GENE_NUM; tmp++)
			deadNum[tmp] = 0;
	}
	
	private void outputGeneToFile(){
		try {
			PrintWriter output = new PrintWriter(new FileWriter(
			"C:/Users/정현우/Desktop/WorkSetting/HelloJava/gene.txt",true));
			output.println("!" + numOfGeneration + "_result");
			output.println("----------");
			for(int iCount = 0; iCount < GENE_NUM; iCount++){
				output.println(iCount+ "개체 죽은 횟수 : " + deadNum[iCount]);
			}
			output.println("----------");
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void inputGeneFromFile(){
		
		try {
			BufferedReader input = new BufferedReader(new FileReader(
					"C:/Users/정현우/Desktop/WorkSetting/HelloJava/gene.txt"));
			limit10 = 0;
			while(true){
				String line = input.readLine();
				if(line == null || limit10 == GENE_NUM) break;
				
				String list[] = line.split("");
				if(gnFound){
					geneList[limit10] = line;
					limit10++;
				}
				if(list[0].equals("#")){
					String sum = "";
					for(int tmp = 1; !(list[tmp].equals("_")); tmp++)
						sum += list[tmp];
					if(Integer.valueOf(sum) == numOfGeneration){
						gnFound = true;
						input.readLine();
					}
				}
			}
			input.close();
			gnFound = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

public class geneProgram {
	public static void main(String[] args) throws IOException {
		/*int random;
		PrintWriter output = new PrintWriter(
				"C:/Users/정현우/Desktop/WorkSetting/HelloJava/gene.txt");
		for(int tmp = 0; tmp < 5; tmp ++){
			random = (int) ((Math.random())*4+1);
			output.print(random);
		}
		output.println();
		output.close();
		
		PrintWriter output2 = new PrintWriter(new FileWriter(
				"C:/Users/정현우/Desktop/WorkSetting/HelloJava/gene.txt"
				,true));
		for(int tmp = 0; tmp < 5; tmp ++){
			random = (int) ((Math.random())*4+1);
			output2.print(random);
		}
		output2.println();
		output2.close();
		
		BufferedReader input = new BufferedReader(new FileReader(
				"C:/Users/정현우/Desktop/WorkSetting/HelloJava/gene.txt"));
		while(true){
			String line = input.readLine();
			if(line == null) break;
			String list[] = line.split("");
			for(int tmp = 0; tmp < list.length; tmp++)
				System.out.print(list[tmp]);
			System.out.println();
		}
		input.close();*/
		
		bombGame geneTest = new bombGame(5, 12, 100);
		geneTest.gameMain();
	}
}
