import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client1 {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 9999;

        // 서버 정보 파일
        try (BufferedReader reader = new BufferedReader(new FileReader("server_info.dat"))) {
            serverAddress = reader.readLine();
            port = Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            System.out.println("서버정보 파일을 읽는 중 오류가 발생했습니다. 기본 주소와 포트를 사용합니다.");
        }

        try (Socket socket = new Socket(serverAddress, port)) {
            System.out.println("서버에 연결되었습니다.");

            // 서버와의 통신을 별도의 스레드에서 처리
            Thread serverResponseThread = new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                        if (response.contains("퀴즈 종료")) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("서버와의 통신 중 오류 발생: " + e.getMessage());
                }
            });
            serverResponseThread.start();

            // 사용자의 입력을 서버로 보내기
            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                 Scanner scanner = new Scanner(System.in)) {

                while (scanner.hasNextLine()) {
                    String userInput = scanner.nextLine();
                    out.write(userInput + "\n");
                    out.flush();
                    if (userInput.equalsIgnoreCase("fin")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("서버로 메시지 전송 중 오류 발생: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("서버에 연결할 수 없습니다: " + e.getMessage());
        }
    }
}