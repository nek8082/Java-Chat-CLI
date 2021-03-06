package com.nek;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import com.nek.threads.ReadingThread;
import com.nek.threads.WritingThread;

class Server {
	private final int port;
	private final boolean safe;

	Server(final int port, final boolean safe) {
		this.port = port;
		this.safe = safe;
	}

	void run() {
		System.out.println("Listening");
		try (final ServerSocket serverSocket = new ServerSocket(port);
				final Socket socket = serverSocket.accept();
				// Get the input stream (sequence of bytes)
				final InputStream inputStream = socket.getInputStream();
				// InputStreamReader takes a byte stream and converts it into a character stream
				final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				// Reads more than 1 char from the stream and safes them into a buffer
				final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				// Get the output stream (sequence of bytes)
				final OutputStream outputStream = socket.getOutputStream();
				// Use PrintWriter to make the output stream buffered and work with characters
				final PrintWriter writer = new PrintWriter(outputStream, true);) {
			Main.connected.set(true);
			System.out.println("Connected\n");
			final String ip = socket.getRemoteSocketAddress().toString().split(":")[0];
			if (safe) {
				DBManager.select(ip);
			}
			final Thread readingThread = new ReadingThread(bufferedReader, ip, safe);
			final Thread writingThread = new WritingThread(writer, ip, safe);

			readingThread.start();
			writingThread.start();

			try {
				readingThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				writingThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Program terminated");
	}
}
