package webapp

object Utils {
    val String.objectName get() = replaceFirst(first(), first().lowercaseChar())
}