package com.paypay.baymax.front.service;

import org.springframework.stereotype.Service;

import com.paypay.baymax.commons.DTO.BitacoraDTO;
import com.paypay.baymax.commons.DTO.pagination.DataTableRequest;

@Service
public class BitacoraService extends ServiceUtils<BitacoraDTO,DataTableRequest<Object>> {
	
	public void saveBitacora(BitacoraDTO bitDTO) {
		super.generateBitacora(bitDTO);
	}
}
