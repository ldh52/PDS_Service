package com.test.sku.network.pds;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileIO 
{
    private static String srcPath = "C:/test/";
    private static String savePath = "C:/test/upfiles/";
    private static String serialPath = "C:/test/data/file_list.ser";

    public byte[] load(String fname) 
    {
        File f = new File(srcPath + fname);
        if (!f.exists()) {
            System.err.println(f.getPath() + " 파일이 없습니다.");
            return null;
        }
        try (FileInputStream fin = new FileInputStream(f)) {
            byte[] buf = new byte[(int) f.length()];
            fin.read(buf);
            fin.close();
            return buf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(String fname, byte[] fdata) {
        if (fname == null || fname.isEmpty() || fdata == null) {
            return false;
        }

        try {
        	FileOutputStream fos = new FileOutputStream(savePath + fname);
            fos.write(fdata);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<PDSVO> deserialize() {
    	
    	File f = new File(serialPath);
    	if(!f.exists()) {
    		return new ArrayList<PDSVO>();
    	}
    	try {
			ObjectInputStream oin = new ObjectInputStream(new FileInputStream(f));
			List<PDSVO> list = (List<PDSVO>)oin.readObject();
			oin.close();
			//System.out.println("Fio.list=" + list.size());
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    public boolean serialize(List<PDSVO> list) {
    	
    	File f = new File(serialPath);

    	try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
			oos.writeObject(list);
			oos.flush();
			oos.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return false;
    }

	public PDSVO find(PDSVO key) {
		List<PDSVO> list = deserialize();
		if(list.contains(key)) {
			return list.get(list.indexOf(key));
		}
		return null;
	}
	
	public boolean update(PDSVO key) {
		List<PDSVO> list = deserialize();
		if(list.contains(key)) {
			list.get(list.indexOf(key)).setDesc(key.getDesc());
			return serialize(list);
		}
		return serialize(list);
	}

	public boolean delete(PDSVO key) {
		List<PDSVO> list = deserialize();
		if(list.contains(key)) {
			PDSVO pds = list.get(list.indexOf(key));
			list.remove(key);
			File f = new File(savePath+pds.getFname());
			if(f.exists()) {
				return f.delete() && serialize(list);
			}
		}
		return false;
	}
}