package models

import java.io.File
import java.nio.file.Files
import java.util.Base64

object Image64 {
  def apply(file: File): String = {
    val bytes = Files.readAllBytes(file.toPath)  
    val enc = Base64.getEncoder.encodeToString(bytes)
    s"data:image/jpeg;base64,${enc}"
  }

  def apply(file: String): String = apply(new File(file))
}