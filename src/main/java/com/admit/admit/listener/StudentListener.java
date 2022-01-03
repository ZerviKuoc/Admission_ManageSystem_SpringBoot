package com.admit.admit.listener;

import com.admit.admit.entity.ClassInfo;
import com.admit.admit.entity.Spe;
import com.admit.admit.entity.StuInfo;
import com.admit.admit.entity.Student;
import com.admit.admit.mapper.find.FindMapper;
import com.admit.admit.mapper.importInfo.ImportMapper;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;


import javax.annotation.Resource;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


//监听，在这里插入学生

public class StudentListener extends AnalysisEventListener<Student> {
    private FindMapper findMapper;
    private ImportMapper importMapper;
    private Random random;
    private HashMap<String,String> depHashMap;
    private HashMap<String,Integer> speHashMap;
    private List<Spe> allSpe;


    public static String getSysYear() {
        Calendar date = Calendar.getInstance();
        String year = String.valueOf(date.get(Calendar.YEAR));
        return year;
    }


    public StudentListener(FindMapper findMapper, Random random, HashMap<String, String> depHashMap, HashMap<String, Integer> speHashMap, List<Spe> allSpe,ImportMapper importMapper){
        this.findMapper = findMapper;
        this.random = random;
        this.depHashMap = depHashMap;
        this.speHashMap = speHashMap;
        this.allSpe = allSpe;
        this.importMapper = importMapper;

        int i = 1;
        for(Spe sp: allSpe) {
            this.depHashMap.put(sp.getDep_name(),sp.getDep_id());
            String[] split = sp.getSpe_names().split(",");
            i = 1;
            for(String ss: split) {
                this.speHashMap.put(ss,i++);
            }
        }
    }


    @Override
    public void invoke(Student st, AnalysisContext analysisContext) {
        String dep_id = this.depHashMap.get(st.getDep());
        String spe_id = String.valueOf(this.speHashMap.get(st.getSpe()));
        String r = String.valueOf((this.random.nextInt(2)+1));
        StuInfo stuInfo = new StuInfo(st.getNumber(),st.getName(),"3",r,dep_id,spe_id,getSysYear());

        String degree = stuInfo.getDegree();
        double maxRate = 0;
        double rate;
        double boy;
        double girl;
        ClassInfo rightClass = null;
        String sno;
        int count;

        // 拿到要分的班级列表
        List<ClassInfo> classInfos = this.importMapper.ForCreateSno(dep_id, spe_id, degree);

        for(ClassInfo classInfo: classInfos) {
            if(classInfo.getBoy()==null) {
                classInfo.setBoy("0");
                boy = 0;
            } else {
                boy = Double.parseDouble(classInfo.getBoy());
            }
            if(classInfo.getGirl()==null) {
                classInfo.setGirl("0");
                girl = 0;
            } else {
                girl = Double.parseDouble(classInfo.getGirl());
            }

            if(Integer.parseInt(stuInfo.getSex())==2) { // 是女孩
                stuInfo.setUrl("D:\\Front end\\DB_Project\\view\\admit\\src\\assets\\avatar\\2.png");
                if(boy==0&&girl==0) {
                    rate = 0;
                } else {
                    rate = boy/girl; // 男女比例, 最大代表男孩子相对女孩子更多
                }
            } else { // 男孩
                stuInfo.setUrl("D:\\Front end\\DB_Project\\view\\admit\\src\\assets\\avatar\\1.png");
                if(boy==0&&girl==0) {
                    rate = 0;
                } else {
                    rate = girl/boy; // 男女比例，最大代表女孩子相对男孩子更多
                }
            }
            if(maxRate <= rate && (boy+girl)<60) { // 求出最大的男女比例
                maxRate = rate;
                rightClass = classInfo; // 拿到合适的班级信息
            }
        }

        if(rightClass ==null) {
            System.out.println(stuInfo.getId()+"没有找到合适的班级");
        }

        count = Integer.parseInt(rightClass.getGirl()) + Integer.parseInt(rightClass.getBoy()) + 1;
        if(count<=9) {
            sno = stuInfo.getDegree()+stuInfo.getSex()+ stuInfo.getYear().substring(2,4) +
                    stuInfo.getDep_id() + stuInfo.getSpe_id() + rightClass.getClass_no() + "0"+count;
        } else {
            sno = stuInfo.getDegree()+stuInfo.getSex()+ stuInfo.getYear().substring(2,4) +
                    stuInfo.getDep_id() + stuInfo.getSpe_id() + rightClass.getClass_no() + count;
        }

        this.importMapper.insertst(stuInfo.getId(),stuInfo.getName(),stuInfo.getDegree(),
                stuInfo.getSex(),stuInfo.getDep_id(),stuInfo.getSpe_id(),stuInfo.getYear(),sno,rightClass.getId(),count);
        if(stuInfo.getUrl()!=null) {
            importMapper.InsertAvatar(sno,stuInfo.getUrl());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}