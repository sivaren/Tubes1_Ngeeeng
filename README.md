# Tubes1_Ngeeeng
Pemanfaatan Algoritma Greedy dalam Aplikasi Permainan “Overdrive”

> _Program Ini Dibuat Untuk Memenuhi Tugas Perkuliahan Mata Kuliah Strategi Algoritma (IF2211)_ <br/>
>
> _Program Studi Teknik Informatika <br/>
> Sekolah Teknik Elektro dan Informatika <br/>
> Institut Teknologi Bandung <br/>
> Semester II Tahun 2021/2022 <br/>_

## Table of Contents
* [General Information](#general-information)
* [Prerequisites](#prerequisites)
* [Build Program](#build-program)
* [Run Program](#run-program)
* [Project Status](#project-status)
* [Author](#author)

## General Information
> Program sederhana dalam Bahasa Java yang mengimplementasikan Algoritma Greedy dalam Aplikasi Permainan “Overdrive”.
> Strategi Greedy yang digunakan adalah Greedy by Speed sehingga prioritas Bot adalah mencapai kecepatan tertinggi.
> Jika terdapat lebih dari satu Command yang menghasilkan kecepatan tertinggi yang sama, maka Command yang dipilih adalah command dengan damage yang terkecil. Jika damage yang dihasilkan sama, maka bot akan memilih untuk menyerang pemain lain.
> Jika kecepatan mobil sudah maksimal, maka bot akan berfokus untuk menyerang pemain lain.

## Prerequisites
1. Java (minimal Java 8)
    ```link download: https://www.oracle.com/java/technologies/downloads/#java8```   
2. Intellij IDEA
    ```link download: https://www.jetbrains.com/idea/```
3. NodeJS
    ```link download: https://nodejs.org/en/download/```
4. **Pastikan branch repository berada di main** </br>
**Clone repository ini menggunakan command berikut (git bash)**

    ```$ git clone https://github.com/sivaren/Tubes1_Ngeeeng.git```

## Build Program 
Build program digunakan jika mengubah kode pada folder src/java/src
Cara Build Program:
1. Buka Intellij IDEA
2. Buka Folder src/java dari tempat code github ini di clone
3.  click maven pada bagian kanan Intelid IDEA (di bawah tombol close program)
4.  click java-starter-bot
5.  click Lifecycle
6.  double click install
7.  program berhasil di bulild pada folder target di src/java


## Run Program
1. download starter pack.zip pada tautan dibawah ini
    ```
    https://github.com/EntelectChallenge/2020-Overdrive/releases/tag/2020.3.4
    ```
2. extract starter pack tersebut di folder yang Anda inginkan
3. buka folder tempat anda meng-extract starter pack tersebut
4. buka starter pack
5. edit file game-runner-config.json
6. ubah konfigurasi player-a atau player-b menjadi path folder src/java github ini di clone:
    misal github ini di clone pada drive D:, maka
    
    before
    ```
    "player-a": "./starter-bot/java",
    "player-b": "./reference-bot/java",
    ```
    after
    ```
    "player-a": "D:/Tubes1_Ngeeeng/src/java",
    "player-b": "./reference-bot/java",
    ```
7. Pada folder yang sama dengan file game-runner-config.json, double click run.bat
8. Game berhasil dijalankan

## Project Status
> **Project is: _in progress_**

## Author
<table>
    <tr>
      <td><b>Nama</b></td>
      <td><b>NIM</b></td>
    </tr>
    <tr>
      <td><a href="https://github.com/bryanbernigen"><b>Bryan Bernigen</b></a></td>
      <td><b>13520034</b></td>
    </tr>
    <tr>
      <td><a href="https://github.com/FelineJTD"><b>Felicia Sutandijo</b></a></td>
      <td><b>13520050</b></td>
    </tr>
    <tr>
      <td><a href="https://github.com/sivaren"><b>Rava Naufal Attar</b></a></td>
      <td><b>13520077</b></td>
    </tr>
</table>
