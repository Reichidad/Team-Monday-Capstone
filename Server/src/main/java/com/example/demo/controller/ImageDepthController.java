package com.example.demo.controller;

import com.example.demo.datatype.PostDetail;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.SavedModelBundle;

import java.io.*;
import java.util.ArrayList;

@AllArgsConstructor
@RestController
public class ImageDepthController {
    static int ROW = 0;
    static int FEATURE = 0;

    @GetMapping("/imageDepth")
    public String imageDetph(){
        ROW = 0;
        FEATURE = 0;
        System.out.println("TensorFlow version : "+TensorFlow.version());

        String filePath = "D:/Capstone/data/test.csv";

        //get shape of data
        try {
            getDataSize(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print("[number of row] ==> "+ ROW);
        System.out.println(" / [number of feature] ==> "+ FEATURE);
        float[][] testInput = new float[ROW][FEATURE];

        //insert csv data to matrix
        try {
            csvToMtrx(filePath, testInput);
        } catch (IOException e) {
            e.printStackTrace();
        }
        printMatrix(testInput);

        //load the model bundle
        try(SavedModelBundle b = SavedModelBundle.load("D:/Capstone/model/test", "serve")){

            //create a session from the Bundle
            Session sess = b.session();

            //create an input Tensor
            Tensor x = Tensor.create(testInput);

            //run the model and get the result
            float[][] y = sess.runner()
                    .feed("x", x)
                    .fetch("h")
                    .run()
                    .get(0)
                    .copyTo(new float[ROW][1]);

            //print out the result
            for(int i=0; i<y.length;i++){
                for(int j =0; j<testInput[i].length; j++) {
                    System.out.print("["+testInput[i][j]+"]");
                }

                System.out.println(" ==> " + (y[i][0]));
            }
        }

        return "Capstone / TensorFlow.version : " + TensorFlow.version();
    }

    @GetMapping("/imageDepth2")
    public String imageDepth(@RequestParam(value = "image") MultipartFile image){
        return "Capstone Image Get Success";
    }

    /**
     * csv 파일의 행/열 사이즈 측정
     * @param filePath
     * @throws IOException
     */
    public void getDataSize(String filePath) throws IOException {
        try {
            //read csv data file
            File csv = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(csv));
            String line = "";
            String[] field = null;

            while((line=br.readLine())!=null) {
                field = line.split(",");
                ROW++;
            }

            FEATURE = field.length;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * csv 파일 데이터를 행렬로 옮김
     * @param filePath
     * @param mtrx
     * @throws IOException
     */
    public void csvToMtrx(String filePath, float[][] mtrx) throws IOException {
        try {
            //read csv data file
            File csv = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(csv));
            String line = "";
            String[] field = null;

            for(int i=0; i<mtrx.length; i++) {
                if((line=br.readLine())!= null) {
                    field = line.split(",");
                    for(int j=0; j<field.length; j++) {
                        mtrx[i][j] = Float.parseFloat(field[j]);
                    }
                }
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 행렬 값 확인용 출력
     * @param mtrx
     */
    public void printMatrix(float[][] mtrx) {
        System.out.println("============ARRAY VALUES============");
        for(int i=0; i<mtrx.length; i++) {
            if(i==0)
                System.out.print("[");
            else
                System.out.println();
            for(int j =0; j<mtrx[i].length; j++) {
                System.out.print("["+mtrx[i][j]+"]");
            }
        }
        System.out.println("]");
    }
}


