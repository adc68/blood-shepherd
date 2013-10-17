/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Alexandre Normand
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.glukit.dexcom.sync;

import jssc.SerialPort;
import org.glukit.dexcom.sync.responses.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.glukit.dexcom.sync.DecodingUtils.fromHexString;
import static org.glukit.dexcom.sync.ResponseReader.HEADER_SIZE;
import static org.glukit.dexcom.sync.ResponseReader.TRAILER_SIZE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link ResponseReader}
 *
 * @author alexandre.normand
 */
@RunWith(MockitoJUnitRunner.class)
public class TestResponseReader {
  @Mock
  SerialPort serialPort;

  @Test
  public void readGenericResponseShouldSucceed() throws Exception {
    when(serialPort.readBytes(HEADER_SIZE)).thenReturn(fromHexString("01 03 01 01"));
    when(serialPort.readBytes(259 - (HEADER_SIZE + TRAILER_SIZE))).thenReturn(fromHexString("3C 46 69 72 6D 77 61 72 65 48 65 61 64 65 72 20 53 63 68 65 6D 61 56 65 72 73 69 6F 6E 3D 27 31 27 20 41 70 69 56 65 72 73 69 6F 6E 3D 27 32 2E 32 2E 30 2E 30 27 20 54 65 73 74 41 70 69 56 65 72 73 69 6F 6E 3D 27 32 2E 34 2E 30 2E 30 27 20 50 72 6F 64 75 63 74 49 64 3D 27 47 34 52 65 63 65 69 76 65 72 27 20 50 72 6F 64 75 63 74 4E 61 6D 65 3D 27 44 65 78 63 6F 6D 20 47 34 20 52 65 63 65 69 76 65 72 27 20 53 6F 66 74 77 61 72 65 4E 75 6D 62 65 72 3D 27 53 57 31 30 30 35 30 27 20 46 69 72 6D 77 61 72 65 56 65 72 73 69 6F 6E 3D 27 32 2E 30 2E 31 2E 31 30 34 27 20 50 6F 72 74 56 65 72 73 69 6F 6E 3D 27 34 2E 36 2E 34 2E 34 35 27 20 52 46 56 65 72 73 69 6F 6E 3D 27 31 2E 30 2E 30 2E 32 37 27 20 44 65 78 42 6F 6F 74 56 65 72 73 69 6F 6E 3D 27 33 27 2F 3E"));
    when(serialPort.readBytes(TRAILER_SIZE)).thenReturn(fromHexString("D8 D4"));

    ResponseReader responseReader = new ResponseReader(new LittleEndianDataInputFactory());
    Utf8PayloadGenericResponse genericResponse = responseReader.read(Utf8PayloadGenericResponse.class, this.serialPort);

    assertThat(genericResponse, not(nullValue()));
    assertThat(genericResponse.asString(), is("<FirmwareHeader SchemaVersion='1' ApiVersion='2.2.0.0' TestApiVersion='2.4.0.0' ProductId='G4Receiver' ProductName='Dexcom G4 Receiver' SoftwareNumber='SW10050' FirmwareVersion='2.0.1.104' PortVersion='4.6.4.45' RFVersion='1.0.0.27' DexBootVersion='3'/>"));
  }

  @Test
  public void readPageRangeResponseShouldMatchExample() throws Exception {
    when(serialPort.readBytes(HEADER_SIZE)).thenReturn(fromHexString("01 0E 00 01"));
    when(serialPort.readBytes(14 - HEADER_SIZE - TRAILER_SIZE)).thenReturn(fromHexString("01 00 00 00 02 00 00 00"));
    when(serialPort.readBytes(TRAILER_SIZE)).thenReturn(fromHexString("97 11"));

    ResponseReader responseReader = new ResponseReader(new LittleEndianDataInputFactory());
    PageRangeResponse pageRangeResponse = responseReader.read(PageRangeResponse.class, this.serialPort);

    assertThat(pageRangeResponse, not(nullValue()));
    assertThat(pageRangeResponse.getFirstPage(), is(1L));
    assertThat(pageRangeResponse.getLastPage(), is(2L));
  }

  @Test
  public void readDatabasePagesShouldSucceed() throws Exception {
    when(serialPort.readBytes(HEADER_SIZE)).thenReturn(fromHexString("01 16 02 01"));
    when(serialPort.readBytes(534 - HEADER_SIZE - TRAILER_SIZE)).thenReturn(fromHexString("00 00 00 00 01 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3A 7D 7D F6 89 07 FD 85 89 07 3C 4D 61 6E 75 66 61 63 74 75 72 69 6E 67 50 61 72 61 6D 65 74 65 72 73 20 53 65 72 69 61 6C 4E 75 6D 62 65 72 3D 22 73 6D 33 30 31 34 30 37 35 32 22 20 48 61 72 64 77 61 72 65 50 61 72 74 4E 75 6D 62 65 72 3D 22 4D 44 31 30 36 30 2D 4D 54 32 30 36 34 39 22 20 48 61 72 64 77 61 72 65 52 65 76 69 73 69 6F 6E 3D 22 31 34 22 20 44 61 74 65 54 69 6D 65 43 72 65 61 74 65 64 3D 22 32 30 31 33 2D 30 31 2D 30 33 20 31 33 3A 35 34 3A 30 35 2E 35 33 36 20 2D 30 38 3A 30 30 22 20 48 61 72 64 77 61 72 65 49 64 3D 22 7B 37 35 42 37 43 38 38 36 2D 46 45 31 30 2D 34 32 30 46 2D 42 35 31 31 2D 32 44 33 46 39 42 39 42 45 45 37 45 7D 22 20 2F 3E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F4 FB"));
    when(serialPort.readBytes(TRAILER_SIZE)).thenReturn(fromHexString("7F 04"));

    ResponseReader responseReader = new ResponseReader(new LittleEndianDataInputFactory());
    DatabasePagesResponse pagesResponse = responseReader.read(DatabasePagesResponse.class, this.serialPort);

    assertThat(pagesResponse, not(nullValue()));
  }

  @Test
  public void readManufacturingDataDatabasePagesShouldSucceed() throws Exception {
    when(serialPort.readBytes(HEADER_SIZE)).thenReturn(fromHexString("01 16 02 01"));
    when(serialPort.readBytes(534 - HEADER_SIZE - TRAILER_SIZE)).thenReturn(fromHexString("00 00 00 00 01 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 3A 7D 7D F6 89 07 FD 85 89 07 3C 4D 61 6E 75 66 61 63 74 75 72 69 6E 67 50 61 72 61 6D 65 74 65 72 73 20 53 65 72 69 61 6C 4E 75 6D 62 65 72 3D 22 73 6D 33 30 31 34 30 37 35 32 22 20 48 61 72 64 77 61 72 65 50 61 72 74 4E 75 6D 62 65 72 3D 22 4D 44 31 30 36 30 2D 4D 54 32 30 36 34 39 22 20 48 61 72 64 77 61 72 65 52 65 76 69 73 69 6F 6E 3D 22 31 34 22 20 44 61 74 65 54 69 6D 65 43 72 65 61 74 65 64 3D 22 32 30 31 33 2D 30 31 2D 30 33 20 31 33 3A 35 34 3A 30 35 2E 35 33 36 20 2D 30 38 3A 30 30 22 20 48 61 72 64 77 61 72 65 49 64 3D 22 7B 37 35 42 37 43 38 38 36 2D 46 45 31 30 2D 34 32 30 46 2D 42 35 31 31 2D 32 44 33 46 39 42 39 42 45 45 37 45 7D 22 20 2F 3E 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 F4 FB"));
    when(serialPort.readBytes(TRAILER_SIZE)).thenReturn(fromHexString("7F 04"));

    ResponseReader responseReader = new ResponseReader(new LittleEndianDataInputFactory());
    ManufacturingDataDatabasePagesResponse pagesResponse =
            responseReader.read(ManufacturingDataDatabasePagesResponse.class, this.serialPort);

    assertThat(pagesResponse, not(nullValue()));
    assertThat(pagesResponse.getManufacturingParameters().size(), is(1));
  }

  @Test
  public void readGlucoseDataDatabasePagesShouldSucceed() throws Exception {
    when(serialPort.readBytes(HEADER_SIZE)).thenReturn(fromHexString("01 46 08 01"));
    byte[] payload = fromHexString("76 D9 00 00 26 00 00 00 04 02 B9 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9F 39 C3 71 D6 08 79 0F D6 08 2F 00 14 CE 1F EF 72 D6 08 A5 10 D6 08 33 00 14 32 0D 1B 74 D6 08 D1 11 D6 08 38 00 14 04 ED 47 75 D6 08 FD 12 D6 08 3C 00 14 DD 4E 73 76 D6 08 29 14 D6 08 45 00 13 25 6A 9F 77 D6 08 55 15 D6 08 4D 00 13 A7 AB CB 78 D6 08 81 16 D6 08 54 00 13 03 2E F6 79 D6 08 AC 17 D6 08 57 00 13 8D 0D 22 7B D6 08 D8 18 D6 08 59 00 14 40 F1 4E 7C D6 08 04 1A D6 08 5A 00 14 C9 31 7A 7D D6 08 30 1B D6 08 5A 00 14 67 2D A6 7E D6 08 5C 1C D6 08 5A 00 14 13 DE D2 7F D6 08 88 1D D6 08 5B 00 14 BB A9 FE 80 D6 08 B4 1E D6 08 5C 00 14 9A DD 2A 82 D6 08 E0 1F D6 08 5C 00 14 E4 A4 56 83 D6 08 0C 21 D6 08 5A 00 94 75 45 82 84 D6 08 38 22 D6 08 5A 00 94 21 C6 83 84 D6 08 39 22 D6 08 5B 80 14 29 1B AE 85 D6 08 64 23 D6 08 59 00 14 53 C5 DA 86 D6 08 90 24 D6 08 58 00 14 66 8F 06 88 D6 08 BC 25 D6 08 55 00 14 75 E6 32 89 D6 08 E8 26 D6 08 55 00 24 D2 F4 5E 8A D6 08 14 28 D6 08 4E 00 24 B9 16 8A 8B D6 08 40 29 D6 08 47 00 25 B8 50 B6 8C D6 08 6C 2A D6 08 41 00 25 11 00 E2 8D D6 08 98 2B D6 08 3E 00 25 3C 70 0E 8F D6 08 C4 2C D6 08 3E 00 24 E6 5A 3A 90 D6 08 F0 2D D6 08 3E 00 24 2A EB 66 91 D6 08 1C 2F D6 08 3E 00 24 86 A7 92 92 D6 08 48 30 D6 08 3F 00 24 D0 B6 BE 93 D6 08 74 31 D6 08 51 00 A4 82 DF EA 94 D6 08 A0 32 D6 08 50 00 B8 12 29 EA 94 D6 08 A0 32 D6 08 5D 80 38 53 E1 16 96 D6 08 CC 33 D6 08 5C 00 38 51 76 42 97 D6 08 F8 34 D6 08 61 00 38 45 8E 6E 98 D6 08 24 36 D6 08 6B 00 38 C3 DE 9A 99 D6 08 50 37 D6 08 79 00 23 A7 5C C6 9A D6 08 7C 38 D6 08 85 00 38 BE C3 FF FF FF FF FF FF 9C D9 00 00 26 00 00 00 04 02 BA 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 D2 E3 F2 9B D6 08 A8 39 D6 08 8A 00 38 C2 EB 1E 9D D6 08 D4 3A D6 08 76 00 B8 E0 13 4A 9E D6 08 00 3C D6 08 74 00 B8 A9 81 4A 9E D6 08 00 3C D6 08 75 80 38 89 3C 76 9F D6 08 2C 3D D6 08 77 00 38 87 53 A2 A0 D6 08 58 3E D6 08 7D 00 38 2E 2D CE A1 D6 08 84 3F D6 08 81 00 38 9B 9F FA A2 D6 08 B0 40 D6 08 81 00 38 80 98 26 A4 D6 08 DC 41 D6 08 7E 00 38 06 EB 52 A5 D6 08 08 43 D6 08 79 00 38 EE E0 7E A6 D6 08 34 44 D6 08 75 00 24 E9 6D AA A7 D6 08 60 45 D6 08 72 00 24 C8 20 D6 A8 D6 08 8C 46 D6 08 70 00 24 2A D4 02 AA D6 08 B8 47 D6 08 6B 00 14 8F 9B 2E AB D6 08 E4 48 D6 08 69 00 14 E6 BE 5A AC D6 08 10 4A D6 08 66 00 14 E6 01 86 AD D6 08 3C 4B D6 08 63 00 14 E5 97 B2 AE D6 08 68 4C D6 08 61 00 14 5A 05 DE AF D6 08 94 4D D6 08 5F 00 14 8E 91 0A B1 D6 08 C0 4E D6 08 5D 00 14 38 7E 36 B2 D6 08 EC 4F D6 08 5C 00 14 74 8D 62 B3 D6 08 18 51 D6 08 59 00 14 17 3D 8E B4 D6 08 44 52 D6 08 58 00 14 0D F4 BA B5 D6 08 70 53 D6 08 56 00 14 A2 F3 E6 B6 D6 08 9C 54 D6 08 54 00 14 E5 4C 12 B8 D6 08 C8 55 D6 08 51 00 14 6F 93 3E B9 D6 08 F4 56 D6 08 4E 00 14 CD 0F 6A BA D6 08 20 58 D6 08 4D 00 14 F6 A7 96 BB D6 08 4C 59 D6 08 4C 00 14 A3 9A C2 BC D6 08 78 5A D6 08 4A 00 14 FD 22 EE BD D6 08 A4 5B D6 08 49 00 14 FE 1B 1A BF D6 08 D0 5C D6 08 47 00 14 EC 30 46 C0 D6 08 FC 5D D6 08 47 00 14 C0 22 72 C1 D6 08 28 5F D6 08 46 00 14 5D DF 9E C2 D6 08 54 60 D6 08 45 00 14 8B 9F CA C3 D6 08 80 61 D6 08 44 00 14 59 42 F6 C4 D6 08 AC 62 D6 08 43 00 14 C0 25 22 C6 D6 08 D8 63 D6 08 43 00 14 48 32 FF FF FF FF FF FF C2 D9 00 00 26 00 00 00 04 02 BB 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6D 4B 4E C7 D6 08 04 65 D6 08 42 00 14 BF E9 7A C8 D6 08 30 66 D6 08 40 00 14 C5 29 A6 C9 D6 08 5C 67 D6 08 40 00 14 DA 89 D2 CA D6 08 88 68 D6 08 3F 00 14 52 63 FE CB D6 08 B4 69 D6 08 3E 00 14 D2 2C 2A CD D6 08 E0 6A D6 08 3C 00 14 B9 1D 56 CE D6 08 0C 6C D6 08 3C 00 14 44 23 82 CF D6 08 38 6D D6 08 3C 00 14 EF 58 AE D0 D6 08 64 6E D6 08 3C 00 14 67 B5 DA D1 D6 08 90 6F D6 08 3C 00 14 09 9B 06 D3 D6 08 BC 70 D6 08 37 00 14 E3 3D 32 D4 D6 08 E8 71 D6 08 37 00 14 E8 E1 5E D5 D6 08 14 73 D6 08 35 00 14 18 0B 8A D6 D6 08 40 74 D6 08 39 00 14 A3 E5 B6 D7 D6 08 6C 75 D6 08 3B 00 14 35 91 E2 D8 D6 08 98 76 D6 08 3B 00 14 95 A7 0E DA D6 08 C4 77 D6 08 39 00 24 BC 48 3A DB D6 08 F0 78 D6 08 39 00 24 B1 D4 66 DC D6 08 1C 7A D6 08 3A 00 24 F2 B2 92 DD D6 08 48 7B D6 08 3B 00 24 09 E7 BE DE D6 08 74 7C D6 08 3E 00 24 22 27 EA DF D6 08 A0 7D D6 08 3E 00 24 C0 CD 16 E1 D6 08 CC 7E D6 08 3D 00 24 9F EE 42 E2 D6 08 F8 7F D6 08 3C 00 14 77 C3 6E E3 D6 08 24 81 D6 08 40 00 14 F2 5A 9A E4 D6 08 50 82 D6 08 43 00 14 60 F7 C6 E5 D6 08 7C 83 D6 08 43 00 14 39 03 F2 E6 D6 08 A8 84 D6 08 42 00 14 2F 63 1E E8 D6 08 D4 85 D6 08 40 00 14 DD 33 4A E9 D6 08 00 87 D6 08 3F 00 14 E6 E3 76 EA D6 08 2C 88 D6 08 3E 00 14 09 90 A2 EB D6 08 58 89 D6 08 3D 00 14 1E 6F CD EC D6 08 84 8A D6 08 3C 00 14 AD FC FA ED D6 08 B0 8B D6 08 3B 00 14 69 1D 25 EF D6 08 DB 8C D6 08 3A 00 14 D6 D7 51 F0 D6 08 07 8E D6 08 3B 00 14 51 50 7D F1 D6 08 33 8F D6 08 3D 00 14 EC 09 A9 F2 D6 08 5F 90 D6 08 41 00 14 B9 E2 FF FF FF FF FF FF E8 D9 00 00 26 00 00 00 04 02 BC 05 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 96 D5 F3 D6 08 8B 91 D6 08 42 00 14 FF 59 01 F5 D6 08 B7 92 D6 08 42 00 14 43 26 2D F6 D6 08 E3 93 D6 08 42 00 14 CE A0 59 F7 D6 08 0F 95 D6 08 44 00 14 97 50 85 F8 D6 08 3B 96 D6 08 46 00 14 66 55 B1 F9 D6 08 67 97 D6 08 47 00 14 4F 5E DE FA D6 08 94 98 D6 08 48 00 14 34 94 09 FC D6 08 BF 99 D6 08 48 00 14 0D 36 35 FD D6 08 EB 9A D6 08 47 00 14 46 9C 61 FE D6 08 17 9C D6 08 47 00 14 34 9D 8D FF D6 08 43 9D D6 08 48 00 14 7D 24 B9 00 D7 08 6F 9E D6 08 47 00 14 16 D2 E5 01 D7 08 9B 9F D6 08 47 00 14 8C F4 12 03 D7 08 C8 A0 D6 08 47 00 14 19 E9 3D 04 D7 08 F3 A1 D6 08 48 00 14 99 AF 69 05 D7 08 1F A3 D6 08 48 00 14 BB 41 95 06 D7 08 4B A4 D6 08 47 00 14 A4 FE C1 07 D7 08 77 A5 D6 08 46 00 14 38 A8 ED 08 D7 08 A3 A6 D6 08 46 00 14 72 E9 19 0A D7 08 CF A7 D6 08 45 00 14 06 A9 45 0B D7 08 FB A8 D6 08 44 00 14 1A 4E 71 0C D7 08 27 AA D6 08 44 00 14 A5 64 9D 0D D7 08 53 AB D6 08 45 00 14 1B A8 C9 0E D7 08 7F AC D6 08 45 00 14 A7 AD F5 0F D7 08 AB AD D6 08 44 00 14 54 3C 21 11 D7 08 D7 AE D6 08 43 00 14 49 C5 4D 12 D7 08 03 B0 D6 08 43 00 14 6F 73 79 13 D7 08 2F B1 D6 08 3F 00 94 F6 F7 79 13 D7 08 2F B1 D6 08 40 80 14 DF 89 A5 14 D7 08 5B B2 D6 08 3E 00 14 78 AD D1 15 D7 08 87 B3 D6 08 3C 00 14 2D 10 FD 16 D7 08 B3 B4 D6 08 3B 00 14 CB 2D 2B 18 D7 08 E1 B5 D6 08 05 00 58 B1 E5 55 19 D7 08 0B B7 D6 08 05 00 58 F1 02 81 1A D7 08 37 B8 D6 08 05 00 58 DE B4 AD 1B D7 08 63 B9 D6 08 05 00 58 D9 EC D9 1C D7 08 8F BA D6 08 05 00 58 9E 9E 05 1E D7 08 BB BB D6 08 05 00 58 74 F6 FF FF FF FF FF FF");
    when(serialPort.readBytes(payload.length)).thenReturn(payload);
    when(serialPort.readBytes(TRAILER_SIZE)).thenReturn(fromHexString("16 8F"));

    ResponseReader responseReader = new ResponseReader(new LittleEndianDataInputFactory());
    GlucoseReadsDatabasePagesResponse pagesResponse =
            responseReader.read(GlucoseReadsDatabasePagesResponse.class, this.serialPort);

    assertThat(pagesResponse, not(nullValue()));
    assertThat(pagesResponse.getGlucoseReads().size(), is(152));
  }
}
