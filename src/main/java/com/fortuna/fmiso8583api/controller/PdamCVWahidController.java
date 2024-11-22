package com.fortuna.fmiso8583api.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.ISO87APackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortuna.fmiso8583api.model.RequestPdamCVWahid;

@RestController
@RequestMapping("/pdam-cvwahid")
public class PdamCVWahidController {

	RequestPdamCVWahid requestPdam = new RequestPdamCVWahid();
	Logger logger = LoggerFactory.getLogger(PdamCVWahidController.class);
//	private ISOSource isoSource;
	private ASCIIChannel channel = null;
	
	
	public PdamCVWahidController(@Value("${cvwahid.ip}") final String ip, @Value("${cvwahid.port}") final int port) {
		logger.info("[CVWAHID-CONNECT] Starting Connection Server");

		connnect(ip, port);
	}

	public void connnect(String ip, Integer port) {
		try {

			ISO87APackager packager = new ISO87APackager();

			// Set up the communication channel
			channel = new ASCIIChannel("202.83.120.219", 2223, packager); // Replace with server IP and port

			// Connect to the server
			channel.connect();
			channel.setTimeout(5000);

			logger.info("[CVWAHID-CONNECT] Connected");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping("/inquiry")
	public String inquiry(@RequestBody String body) throws JsonMappingException, JsonProcessingException {

		String respon = "";
		ObjectMapper mapper = new ObjectMapper();
		requestPdam = mapper.readValue(body, RequestPdamCVWahid.class);

		logger.info("[CVWAHID-Inquiry] Start Request Inquiry");
		logger.info("[CVWAHID-Inquiry] ## idPelanggan " + requestPdam.getBillingId());
		logger.info("[CVWAHID-Inquiry] ## bit37 " + requestPdam.getBit37());
		logger.info("[CVWAHID-Payment] ## kdCA " + requestPdam.getKdCA());


		try {

			ISOMsg packageMsg = new ISOMsg();

			Date date = new Date();
			SimpleDateFormat bulantanggal = new SimpleDateFormat("MMdd");
			SimpleDateFormat jam = new SimpleDateFormat("HH");
			SimpleDateFormat menitdetik = new SimpleDateFormat("mmss");
			SimpleDateFormat jammenitdetik = new SimpleDateFormat("HHmmss");

			String formattedDate = jam.format(new Date());

			Integer jamdikurangi7 = Integer.parseInt(formattedDate) - 7;

			String hasiljamdikurangi7 = "";

			if (String.valueOf(jamdikurangi7).length() == 1) {
				hasiljamdikurangi7 = "0" + jamdikurangi7;
			}

			Calendar calTambah = Calendar.getInstance();
			calTambah.add(Calendar.DAY_OF_MONTH, 1);

			String formattedDate1 = menitdetik.format(new Date());
			String hasil = bulantanggal.format(date) + hasiljamdikurangi7 + formattedDate1;

			Random randomInt = new Random();
			Integer intRand = randomInt.nextInt(999999);
			String strRand = String.format("%6s", intRand).replace(' ', '0');

			ISOPackager packager = channel.getPackager();

			packageMsg.setPackager(packager);
			packageMsg.set(0, "0200");
			packageMsg.set(3, "321066");
			packageMsg.set(7, hasil);
			packageMsg.set(11, strRand);
			packageMsg.set(12, jammenitdetik.format(date));
			packageMsg.set(13, bulantanggal.format(date));
			packageMsg.set(15, bulantanggal.format(calTambah.getTime()));
			packageMsg.set(18, "6012");
			packageMsg.set(32, "017");
			packageMsg.set(37, requestPdam.getBit37());
			packageMsg.set(41, "10000021");
			packageMsg.set(42, requestPdam.getKdCA()+".10000021");
			packageMsg.set(60, requestPdam.getKdCA());
			packageMsg.set(61, requestPdam.getBillingId());
			packageMsg.set(63, "1041");

			channel.send(packageMsg);

			System.out.println("ISO8583 message sent!");

			logger.info("[CVWAHID-Inquiry] Request ISO : [" + new String(packageMsg.pack()) + "]");
			logger.info("[CVWAHID-Inquiry] Request DUMP : [" + dumpLog(packageMsg) + "]");

			// Wait for response
			logger.info("[CVWAHID-Inquiry] Waiting for response ...");
			ISOMsg response = channel.receive();
			logger.info("[CVWAHID-Inquiry]Response received : [" + dumpLog(response) + "]");

			respon = dumpLog(response);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("[CVWAHID-Inquiry] Receive error : " + e.getMessage());
			respon = "error exception";
		}
		
		return respon;

	}
	
	@RequestMapping("/payment")
    public String payment(@RequestBody String body) throws JsonMappingException, JsonProcessingException {

		String respon = "";
		ObjectMapper mapper = new ObjectMapper();
		requestPdam = mapper.readValue(body, RequestPdamCVWahid.class);

		logger.info("[CVWAHID-Payment] Start Request Payment");
		logger.info("[CVWAHID-Payment] ## idPelanggan " + requestPdam.getBillingId());

		logger.info("[CVWAHID-Inquiry] ## bit37 " + requestPdam.getBit37());
		logger.info("[CVWAHID-Payment] ## kdCA " + requestPdam.getKdCA());
		logger.info("[CVWAHID-Payment] ## Amount " + requestPdam.getAmount());
		logger.info("[CVWAHID-Payment] ## bit62 " + requestPdam.getBit62());

		try {

			ISOMsg packageMsg = new ISOMsg();

			Date date = new Date();
			SimpleDateFormat bulantanggal = new SimpleDateFormat("MMdd");
			SimpleDateFormat jam = new SimpleDateFormat("HH");
			SimpleDateFormat menitdetik = new SimpleDateFormat("mmss");
			SimpleDateFormat jammenitdetik = new SimpleDateFormat("HHmmss");

			String formattedDate = jam.format(new Date());

			Integer jamdikurangi7 = Integer.parseInt(formattedDate) - 7;

			String hasiljamdikurangi7 = "";

			if (String.valueOf(jamdikurangi7).length() == 1) {
				hasiljamdikurangi7 = "0" + jamdikurangi7;
			}

			Calendar calTambah = Calendar.getInstance();
			calTambah.add(Calendar.DAY_OF_MONTH, 1);

			String formattedDate1 = menitdetik.format(new Date());
			String hasil = bulantanggal.format(date) + hasiljamdikurangi7 + formattedDate1;

			Random randomInt = new Random();
			Integer intRand = randomInt.nextInt(999999);
			String strRand = String.format("%6s", intRand).replace(' ', '0');

			ISOPackager packager = channel.getPackager();

			packageMsg.setPackager(packager);
			packageMsg.set(0, "0200");
			packageMsg.set(3, "521066");
			packageMsg.set(4, ISOUtil.padleft(requestPdam.getAmount(), 12, '0'));
			packageMsg.set(7, hasil);
			packageMsg.set(11, strRand);
			packageMsg.set(12, jammenitdetik.format(date));
			packageMsg.set(13, bulantanggal.format(date));
			packageMsg.set(15, bulantanggal.format(calTambah.getTime()));
			packageMsg.set(18, "6012");
			packageMsg.set(32, "017");
			packageMsg.set(37, requestPdam.getBit37());
			packageMsg.set(41, "10000021");
			packageMsg.set(42, requestPdam.getKdCA()+".10000021");
			//BIT47 dari BIT37
			packageMsg.set(47, requestPdam.getBit37());
			packageMsg.set(60, requestPdam.getKdCA());
			packageMsg.set(61, requestPdam.getBillingId());
			packageMsg.set(62, requestPdam.getBit62());
			packageMsg.set(63, "1041");

			channel.send(packageMsg);

			System.out.println("ISO8583 message sent!");

			logger.info("[CVWAHID-Payment] Request ISO : [" + new String(packageMsg.pack()) + "]");
			logger.info("[CVWAHID-Payment] Request DUMP : [" + dumpLog(packageMsg) + "]");

			// Wait for response
			logger.info("[CVWAHID-Payment] Waiting for response ...");
			ISOMsg response = channel.receive();
			logger.info("[CVWAHID-Payment]Response received : [" + dumpLog(response) + "]");

			respon = dumpLog(response);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("[CVWAHID-Payment] Receive error : " + e.getMessage());
			respon = "error exception";
		}
		
		return respon;
	
	}
	
	@RequestMapping("/reversal")
    public String reversal(@RequestBody String body) throws JsonMappingException, JsonProcessingException {

		String respon = "";
		ObjectMapper mapper = new ObjectMapper();
		requestPdam = mapper.readValue(body, RequestPdamCVWahid.class);

		logger.info("[CVWAHID-Reversal] Start Request Payment");
		logger.info("[CVWAHID-Reversal] ## idPelanggan " + requestPdam.getBillingId());

		logger.info("[CVWAHID-Inquiry] ## bit47 " + requestPdam.getBit47());
		logger.info("[CVWAHID-Reversal] ## kdCA " + requestPdam.getKdCA());
		logger.info("[CVWAHID-Reversal] ## Amount " + requestPdam.getAmount());

		try {

			ISOMsg packageMsg = new ISOMsg();

			Date date = new Date();
			SimpleDateFormat bulantanggal = new SimpleDateFormat("MMdd");
			SimpleDateFormat jam = new SimpleDateFormat("HH");
			SimpleDateFormat menitdetik = new SimpleDateFormat("mmss");
			SimpleDateFormat jammenitdetik = new SimpleDateFormat("HHmmss");

			String formattedDate = jam.format(new Date());

			Integer jamdikurangi7 = Integer.parseInt(formattedDate) - 7;

			String hasiljamdikurangi7 = "";

			if (String.valueOf(jamdikurangi7).length() == 1) {
				hasiljamdikurangi7 = "0" + jamdikurangi7;
			}

			Calendar calTambah = Calendar.getInstance();
			calTambah.add(Calendar.DAY_OF_MONTH, 1);

			String formattedDate1 = menitdetik.format(new Date());
			String hasil = bulantanggal.format(date) + hasiljamdikurangi7 + formattedDate1;

			Random randomInt = new Random();
			Integer intRand = randomInt.nextInt(999999);
			String strRand = String.format("%6s", intRand).replace(' ', '0');

			ISOPackager packager = channel.getPackager();

			packageMsg.setPackager(packager);
			packageMsg.set(0, "0200");
			packageMsg.set(3, "521067");
			packageMsg.set(4, ISOUtil.padleft(requestPdam.getAmount(), 12, '0'));
			packageMsg.set(7, hasil);
			packageMsg.set(11, strRand);
			packageMsg.set(12, jammenitdetik.format(date));
			packageMsg.set(13, bulantanggal.format(date));
			packageMsg.set(15, bulantanggal.format(calTambah.getTime()));
			packageMsg.set(18, "6012");
			packageMsg.set(32, "017");
			packageMsg.set(41, "10000021");
			packageMsg.set(42, requestPdam.getKdCA()+".10000021");
			packageMsg.set(47, requestPdam.getBit47());
			packageMsg.set(60, requestPdam.getKdCA());
			packageMsg.set(61, requestPdam.getBillingId());
			packageMsg.set(63, "1041");

			channel.send(packageMsg);

			System.out.println("ISO8583 message sent!");

			logger.info("[CVWAHID-Reversal] Request ISO : [" + new String(packageMsg.pack()) + "]");
			logger.info("[CVWAHID-Reversal] Request DUMP : [" + dumpLog(packageMsg) + "]");

			// Wait for response
			logger.info("[CVWAHID-Reversal] Waiting for response ...");
			ISOMsg response = channel.receive();
			logger.info("[CVWAHID-Reversal]Response received : [" + dumpLog(response) + "]");

			respon = dumpLog(response);
		} catch (Exception e) {

			e.printStackTrace();
			logger.error("[CVWAHID-Reversal] Receive error : " + e.getMessage());
			respon = "error exception";
		}
		
		return respon;
	
	
}
	
	public String dumpLog(ISOMsg m) {

		ByteArrayOutputStream bLog = new ByteArrayOutputStream();
		PrintStream pLog = new PrintStream(bLog);
		m.dump(pLog, "");
		return bLog.toString();

	}
}
