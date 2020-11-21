import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;

public class DataCreater{

    public static void main(String[] args) throws IOException{
        makeUsers();
        makeTattooists();
        makePosts();
        makeImages();
    }

    private static void makeUsers() throws IOException {
        BufferedWriter bw = null;
        ArrayList<String> columns = new ArrayList<>();
        columns.add("user_id");
        columns.add("user_pw");
        columns.add("name");
        columns.add("nick_name");

        try {
            bw = Files.newBufferedWriter(
                    Paths.get("D:\\Capstone\\2020_2\\data\\users.csv"),
                    Charset.forName("UTF-8"));

            if (bw != null)
                System.out.println("make file successfully");

            // write column name
            for (int columnIdx = 0; columnIdx < columns.size(); columnIdx++) {
                bw.write(columns.get(columnIdx));
                if (columnIdx != columns.size() - 1)
                    bw.write(",");
            }

            bw.newLine();

            for (int i = 1; i < 10; i++) {
                bw.write("user" + String.valueOf(i));
                bw.write(",");
                bw.write("1234");
                bw.write(",");
                bw.write("name" + String.valueOf(i));
                bw.write(",");
                bw.write("nick_name" + String.valueOf(i));

                bw.newLine();
            }
        }finally {
            bw.close();
        }
    }

    private static void makeTattooists() throws IOException {
        BufferedWriter bw = null;
        ArrayList<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("nick_name");
        columns.add("big_address");
        columns.add("small_address");
        columns.add("mobile");
        columns.add("description");

        try {
            bw = Files.newBufferedWriter(
                    Paths.get("D:\\Capstone\\2020_2\\data\\tattoists.csv"),
                    Charset.forName("UTF-8"));

            if (bw != null)
                System.out.println("make file successfully");

            // write column name
            for (int columnIdx = 0; columnIdx < columns.size(); columnIdx++) {
                bw.write(columns.get(columnIdx));
                if (columnIdx != columns.size() - 1)
                    bw.write(",");
            }

            bw.newLine();

            for (int i = 1; i < 5; i++) {
                bw.write("user" + String.valueOf(i));
                bw.write(",");
                bw.write("nick_name" + String.valueOf(i));
                bw.write(",");
                bw.write("Seoul");
                bw.write(",");
                bw.write("Heukseok");
                bw.write(",");
                bw.write("010-1234-5678");
                bw.write(",");
                bw.write("I'm Tattooist");
                bw.newLine();
            }
        }finally {
            bw.close();
        }
    }

    private static void makePosts() throws IOException {
        BufferedWriter bw = null;
        ArrayList<String> columns = new ArrayList<>();
        columns.add("tattooist_id");
        columns.add("title");
        columns.add("descriptions");
        columns.add("price");
        columns.add("like_num");
        columns.add("genre");
        columns.add("big_shape");
        columns.add("small_shape");
        columns.add("design_url");
        columns.add("avg_clean_score");


        try {
            bw = Files.newBufferedWriter(
                    Paths.get("D:\\Capstone\\2020_2\\data\\posts.csv"),
                    Charset.forName("UTF-8"));

            if (bw != null)
                System.out.println("make file successfully");

            // write column name
            for (int columnIdx = 0; columnIdx < columns.size(); columnIdx++) {
                bw.write(columns.get(columnIdx));
                if (columnIdx != columns.size() - 1)
                    bw.write(",");
            }

            bw.newLine();
            String[] genres = {"rose", "dragon", "geometric"};
            String[] urls = {"", "https://kr.object.ncloudstorage.com/tattoo/design1.png", "https://kr.object.ncloudstorage.com/tattoo/design2.png"};
            for (int i = 1; i < 30; i++) {
                bw.write("user" + String.valueOf(i%3));
                bw.write(",");
                bw.write("Tattoo Title " + String.valueOf(i));
                bw.write(",");
                bw.write("This is a tattoo");
                bw.write(",");
                bw.write(String.valueOf(0));
                bw.write(",");
                bw.write(String.valueOf(0));
                bw.write(",");
                bw.write(genres[i/10]);
                bw.write(",");
                bw.write(genres[i/10]);
                bw.write(",");
                bw.write(genres[i/10]);
                bw.write(",");
                bw.write(urls[i%3]);
                bw.write(",");
                bw.write(String.valueOf(0));

                bw.newLine();
            }
        }finally {
            bw.close();
        }
    }

    private static void makeReviews() {
        BufferedWriter bw = null;
        ArrayList<String> columns = new ArrayList<>();
        columns.add("user_id");
        columns.add("post_id");
        columns.add("nick_name");
        columns.add("date");
        columns.add("description");
        columns.add("tattoo_url1");
        columns.add("tattoo_url2");

    }

    private static void makeImages() throws IOException{
        BufferedWriter bw = null;
        ArrayList<String> columns = new ArrayList<>();
        columns.add("post_id");
        columns.add("filename");
        columns.add("url");


        try {
            bw = Files.newBufferedWriter(
                    Paths.get("D:\\Capstone\\2020_2\\data\\images.csv"),
                    Charset.forName("UTF-8"));

            if (bw != null)
                System.out.println("make file successfully");

            // write column name
            for (int columnIdx = 0; columnIdx < columns.size(); columnIdx++) {
                bw.write(columns.get(columnIdx));
                if (columnIdx != columns.size() - 1)
                    bw.write(",");
            }

            bw.newLine();
            String[] genres = {"rose", "dragon", "geometric"};
            for (int i = 1; i < 30; i++) {
                for (int j = 0 ; j < 2; j++) {
                    bw.write(String.valueOf(i));
                    bw.write(",");
                    bw.write(genres[i / 10] + String.valueOf(2*(i-1)+j+1));
                    bw.write(",");
                    bw.write("https://kr.object.ncloudstorage.com/tattoo/" + genres[i / 10] + "%20%28" + String.valueOf(2*(i-1)+j+1) + "%29.jpg");
                    bw.newLine();
                }
            }
        }finally {
            bw.close();
        }
    }


}
