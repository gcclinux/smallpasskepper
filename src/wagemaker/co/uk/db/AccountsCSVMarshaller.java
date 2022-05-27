package wagemaker.co.uk.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import wagemaker.co.uk.util.Translator;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class AccountsCSVMarshaller {

    public void marshal(ArrayList<?> accounts, File file) throws ExportException {

        if (file.exists()) {
            throw new ExportException("The file to export to already exists");
        }

        try {
            FileWriter writer = new FileWriter(file+".csv");
    
            CsvWriter csvWriter = new CsvWriter(writer, ',');
            for (int i=0; i<accounts.size(); i++) {
                csvWriter.writeRecord(
                        getAccountAsStringArray(
                                (AccountInformation) accounts.get(i)
                        )
                );
            }
            csvWriter.close();
        } catch (IOException e) {
            throw new ExportException(e);
        }

    }

    public ArrayList<AccountInformation> unmarshal(File file) throws ImportException {
        ArrayList<AccountInformation> accounts = new ArrayList<AccountInformation>();

        try {
            CsvReader csvReader = new CsvReader(new FileReader(file));
            while (csvReader.readRecord()) {
                if (csvReader.getColumnCount() != 5) {
                    throw new ImportException(
                            Translator.translate("notCSVFileError", 
                                    new Object[] {file.getAbsoluteFile(), 
                                    new Long(csvReader.getCurrentRecord() + 1)})); 
                }
                accounts.add(new AccountInformation(
                        csvReader.get(0),
                        csvReader.get(1),
                        csvReader.get(2),
                        csvReader.get(3),
                        csvReader.get(4)));
            }
        } catch (FileNotFoundException e) {
            throw new ImportException(e);
        } catch (IOException e) {
            throw new ImportException(e);
        }

        return accounts;
    }

    private String[] getAccountAsStringArray(AccountInformation account) {
        String[] arr = new String[5];
        arr[0] = account.getAccountName();
        arr[1] = new String(account.getUserId());
        arr[2] = new String(account.getPassword());
        arr[3] = new String(account.getUrl());
        arr[4] = new String(account.getNotes());
        return arr;
    }

}
