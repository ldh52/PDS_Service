package com.test.sku.network.pds;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import static com.test.sku.network.pds.PDSRequest.CMD;

public class ServerThread extends Thread 
{

    private Socket clientSocket;
    private ObjectInputStream oin;
    private ObjectOutputStream oos;

    public ServerThread(Socket clientSocket) 
    {
        this.clientSocket = clientSocket;
        try {
            oin = new ObjectInputStream(clientSocket.getInputStream());
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() 
    {
        try {
            while (true) 
            {
            	PDSRequest request = (PDSRequest) oin.readObject();
            	switch(request.getCmd()) 
            	{
            	case UPLOAD: 
            		PDSResponse res = new PDSResponse(upload(request) ? "업로드 성공":"업로드 실패");
            		oos.writeObject(res);
            		oos.flush();
            		break;
            	case LIST: //직렬화 파일 로드, 리스트 추출, res 오브젝트에 설정, res오브젝트 전송
            		res = new PDSResponse(new FileIO().deserialize());
            		oos.writeObject(res);
            		oos.flush();
            		break;
            	case DETAIL:  // 파일번호/파일이름으로 검색
            		oos.writeObject(new PDSResponse(find(request.getPds())));
            		oos.flush();
            		break;
            	case UPDATE:
            		boolean updated = new FileIO().update(request.getPds());
            		oos.writeObject(new PDSResponse(updated ? "수정성공":"수정실패"));
            		oos.flush();
            		break;
            	case DELETE:
            		boolean deleted = new FileIO().delete(request.getPds());
            		oos.writeObject(new PDSResponse(deleted ? "삭제성공":"삭제실패"));
            		oos.flush();
            		break;
            	}
            }
        } catch (Exception e) {
        	System.err.println("클라이언트가 접속 해제");
        	//e.printStackTrace();
        }
        System.err.println("클라이언트를 위한 서버측 쓰레드 종료");
    }
    
    private PDSVO find(PDSVO key)
    {
    	PDSVO found = new FileIO().find(key);
    	return found;
	}
    
    private boolean upload(PDSRequest req)
    {
    	PDSVO pds = req.getPds();
		FileIO fio = new FileIO();
		boolean saved = fio.save(pds.getFname(), pds.getFdata());
		if(!saved) return false;
		
		pds.setFsize(pds.getFdata().length);
		List<PDSVO> list = fio.deserialize();
		if(list.size()==0) {
			pds.setNo(1);
		}else {
    		//Collections.sort(list);
    		PDSVO last = list.get(list.size()-1);
    		pds.setNo(last.getNo()+1);
    		pds.setFdata(null);
		}
		list.add(pds);
		return fio.serialize(list);
    }
}