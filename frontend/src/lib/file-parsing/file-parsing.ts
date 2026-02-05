import Papa from 'papaparse'
import * as XLSX from 'xlsx'
import * as pdfjsLib from 'pdfjs-dist'

/* ----------------------------------
   Shared Types
----------------------------------- */

export type ParsedRow = Record<string, unknown>

export type ParsedText = {
  raw_text: string
}

/* ----------------------------------
   CSV
----------------------------------- */

export function parseCSV(file: File): Promise<ParsedRow[]> {
  return new Promise((resolve, reject) => {
    Papa.parse<ParsedRow>(file, {
      header: true,
      skipEmptyLines: true,
      complete: (results) => resolve(results.data),
      error: (error) => reject(error),
    })
  })
}

/* ----------------------------------
   Excel
----------------------------------- */

export function parseExcel(file: File): Promise<ParsedRow[]> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()

    reader.onload = (e: ProgressEvent<FileReader>) => {
      if (!e.target?.result) {
        reject(new Error('Failed to read file'))
        return
      }

      const data = new Uint8Array(e.target.result as ArrayBuffer)
      const workbook = XLSX.read(data, { type: 'array' })

      const sheetName = workbook.SheetNames[0]
      const sheet = workbook.Sheets[sheetName]

      const json = XLSX.utils.sheet_to_json<ParsedRow>(sheet)
      resolve(json)
    }

    reader.onerror = () => reject(reader.error)
    reader.readAsArrayBuffer(file)
  })
}

/* ----------------------------------
   TXT
----------------------------------- */

export function parseTXT(file: File): Promise<ParsedText> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()

    reader.onload = () => {
      resolve({
        raw_text: String(reader.result ?? ''),
      })
    }

    reader.onerror = () => reject(reader.error)
    reader.readAsText(file)
  })
}

/* ----------------------------------
   PDF
----------------------------------- */

// Required for pdfjs to work in browser
pdfjsLib.GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjsLib.version}/pdf.worker.min.js`

export async function parsePDF(file: File): Promise<ParsedText> {
  const arrayBuffer = await file.arrayBuffer()

  const pdf = await pdfjsLib.getDocument({
    data: arrayBuffer,
  }).promise

  let text = ''

  for (let i = 1; i <= pdf.numPages; i++) {
    const page = await pdf.getPage(i)
    const content = await page.getTextContent()

    text += content.items
      .map((item) => {
        if ('str' in item) {
          return item.str
        }
        return ''
      })
      .join(' ')
  }

  return { raw_text: text }
}