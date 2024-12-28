package com.test.sku.network.pds;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import static com.test.sku.network.pds.PDSRequest.CMD;

public class PDSClient 
{
    private static InputStream in;
    private static ObjectInputStream oin;
    private static OutputStream out;
    private static ObjectOutputStream oos;

    private static Scanner kbd = new Scanner(System.in);
    
    public static void main(String[] args) 
    {
        try (Socket socket = new Socket("localhost", 1234)) {
            System.out.println("서버에 접속");

            out = socket.getOutputStream();
            oos = new ObjectOutputStream(out);

            in = socket.getInputStream();
            oin = new ObjectInputStream(in);

            while (true) {
                System.out.println("업로드(a), 목록(s), 상세(i), 검색(f), 수정(u), 삭제(d), 종료(x):");
                String m = kbd.nextLine().trim();

                if (m.equals("a")) {
                    
                	PDSVO pds = input();
                    
                    PDSRequest req = new PDSRequest(CMD.UPLOAD);
                    req.setPds(pds);
                    
                    oos.writeObject(req);
                    oos.flush();

                    PDSResponse res = (PDSResponse) oin.readObject();
                    System.out.println("서버 측 응답: " + res.getMsg());

                } else if (m.equals("s")) {
                    PDSRequest req = new PDSRequest(CMD.LIST);
                    oos.writeObject(req);
                    oos.flush();

                    PDSResponse res = (PDSResponse) oin.readObject();
                    if(res.getList()==null) {
                    	System.out.println("아직 목록 아이템이 없습니다");
                    	continue;
                    }
                    for (PDSVO fileInfo : res.getList()) {
                    	fileInfo.setDesc("");
                        System.out.println(fileInfo);
                    }
                } 
                else if(m.equals("i")) { //상세보기
                	System.out.println("목록번호:");
                	int no = kbd.nextInt();
                	kbd.nextLine();
                	PDSRequest req = new PDSRequest(CMD.DETAIL);
                	PDSVO pds = new PDSVO(no);
                	req.setPds(pds);
                	oos.writeObject(req);
                	oos.flush();
                	
                	PDSResponse res = (PDSResponse)oin.readObject();
                	System.out.println(res.getPds());
                }
                else if (m.equals("f")) {
                    System.out.println("검색할 파일명:");
                    String fname = kbd.nextLine().trim();

                    PDSRequest req = new PDSRequest(CMD.DETAIL);
                    PDSVO pds = new PDSVO(fname);
                    req.setPds(pds);

                    oos.writeObject(req);
                    oos.flush();

                    PDSResponse res = (PDSResponse) oin.readObject();
                    pds = res.getPds();
                    if (pds != null) {
                        System.out.println(pds);
                    } else {
                        System.err.printf("파일(%s)을 찾을 수 없습니다%n", fname);
                    }

                } else if (m.equals("u")) {
                    System.out.println("업데이트할 파일번호:");
                    int no = kbd.nextInt();
                    kbd.nextLine();

                    System.out.println("새 설명:");
                    String newDesc = kbd.nextLine().trim();

                    PDSRequest req = new PDSRequest(CMD.UPDATE);
                    PDSVO pds = new PDSVO(no);
                    pds.setDesc(newDesc);
                    req.setPds(pds);
                    
                    oos.writeObject(req);
                    oos.flush();

                    PDSResponse res = (PDSResponse) oin.readObject();
                    System.out.println(res.getMsg());

                } else if (m.equals("d")) {
                    System.out.println("삭제할 파일번호:");
                    int no = kbd.nextInt();
                    kbd.nextLine();

                    PDSRequest req = new PDSRequest(CMD.DELETE);
                    PDSVO pds = new PDSVO(no);
                    req.setPds(pds);
                    oos.writeObject(req);
                    oos.flush();

                    PDSResponse response = (PDSResponse) oin.readObject();
                    System.out.println(response.getMsg());

                } else if (m.equals("x")) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("클라이언트 종료");
        }
    }
    
    private static PDSVO input() {
    	System.out.println("파일명:");
        String fname = kbd.nextLine().trim();

        System.out.println("작성자:");
        String author = kbd.nextLine().trim();

        System.out.println("설명:");
        String desc = kbd.nextLine().trim();

        byte[] fdata = new FileIO().load(fname);
        if (fdata == null) {
            System.err.println("파일을 읽을 수 없습니다("+fname+")");
            return null;
        }

        PDSVO pds = new PDSVO();  // 파일관련 모든 속성
        pds.setFname(fname);
        pds.setAuthor(author);
        pds.setFdata(fdata);
        pds.setDate(new Date());
        pds.setDesc(desc);
        pds.setKey("no");
        
        return pds;
    }
}