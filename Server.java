import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    public static void main(String[] args) {
        ServerSocket listener = null;
        ExecutorService pool = Executors.newFixedThreadPool(10); // 최대 10명의 클라이언트 동시 접속 처리

        try {
            listener = new ServerSocket(9999);
            System.out.println("퀴즈 서버가 실행 중입니다. 클라이언트의 연결을 기다립니다...");

            while (true) {
                Socket socket = listener.accept();
                System.out.println("클라이언트가 연결되었습니다: " + socket);
                pool.execute(new QuizHandler(socket)); // 클라이언트마다 스레드 생성
            }
        } catch (IOException e) {
            System.out.println("서버 오류: " + e.getMessage());
        } finally {
            if (listener != null) {
                try {
                    listener.close();
                } catch (IOException e) {
                    System.out.println("서버 소켓을 닫는 중 오류 발생: " + e.getMessage());
                }
            }
            pool.shutdown();
        }
    }
}

class QuizHandler implements Runnable {
    private Socket socket;
    private int totalScore = 0;

    public QuizHandler(Socket socket) {
        this.socket = socket;
    }

    private String game(String exp) {
        StringTokenizer st = new StringTokenizer(exp, " ");
        if (st.countTokens() != 2) {
            totalScore -= 1;
            return "형식이 틀립니다.";
        }
        String res;
        String op1 = st.nextToken();
        String op2 = st.nextToken();

        switch (op1) {
            case "1번":
                if (Objects.equals(op2, "아메리카노")) {
                    res = "-정답";
                    totalScore += 2;
                } else {
                    res = "-오답";
                }
                break;
            case "2번":
                if (Objects.equals(op2, "144")) {
                    res = "-정답";
                    totalScore += 2;
                } else {
                    res = "-오답";
                }
                break;
            case "3번":
                if (Objects.equals(op2, "1")) {
                    res = "-정답";
                    totalScore += 2;
                } else {
                    res = "-오답";
                }
                break;
            case "4번":
                if (Objects.equals(op2, "나머지")) {
                    res = "-정답";
                    totalScore += 2;
                } else {
                    res = "-오답";
                }
                break;
            case "5번":
                if (Objects.equals(op2, "서울")) {
                    res = "-정답";
                    totalScore += 2;
                } else {
                    res = "-오답";
                }
                break;
            default:
                res = "--형식이 맞지 않습니다--";
                totalScore -= 1;
        }
        return res;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            out.write("퀴즈를 시작하고 싶으면 1을 입력하세요!\n");
            out.flush();

            String initMessage = in.readLine();
            if (!"1".equals(initMessage)) {
                System.out.println("클라이언트가 '1'을 입력하지 않음. 종료합니다.");
                return;
            }

            System.out.println("클라이언트가 퀴즈 시작을 요청했습니다.");

            String[] questions = {
                    "1번. 한국인들이 좋아하는 음료로, 에스프레소를 물에 희석해서 먹는 음료의 이름은?",
                    "2번. 12 * 12의 사칙연산 결과는?",
                    "3번. 전통적인 전화 회선으로 사용된 통신 방법으로 옳은 것은? 1.TCP 2.UDP",
                    "4번. 프로그래밍 언어에서 %는 어떤 연산인가? '000연산' 빈칸에 들어갈 말을 적으시오.",
                    "5번. 한국의 수도는?", "(정답은 +2점, 오답은 +0점, 형식오류는 -1점)\n"
            };

            for (String question : questions) {
                out.write(question + "\n");
                out.flush();
            }

            out.write("답을 'n번 답' 형식으로 정답을 입력하세요:\n");
            out.flush();

            int questionCount = 0;
            while (questionCount < 5) {
                String inputMessage = in.readLine();
                if (inputMessage == null || inputMessage.equalsIgnoreCase("fin")) {
                    System.out.println("클라이언트에서 연결을 종료하였습니다.");
                    break;
                }
                System.out.println("클라이언트 답변: " + inputMessage);
                String res = game(inputMessage);
                out.write(res + "\n");
                out.flush();
                questionCount++;
            }

            out.write("퀴즈 종료! 최종 점수는: " + totalScore + "점입니다\n");
            out.flush();

        } catch (IOException e) {
            System.out.println("클라이언트와의 통신 중 오류 발생: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("소켓 닫기 오류: " + e.getMessage());
            }
            System.out.println("클라이언트 연결 종료: " + socket);
        }
    }
}
