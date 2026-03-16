type Props = {
  status: "Pending" | "Accepted" | "Rejected"
}

export function ContractStatusBadge({ status }: Props) {
  let classes = ""
  
  if (status === "Pending") {
    classes = "bg-amber-100 text-amber-700"
  } else if (status === "Accepted") {
    classes = "bg-emerald-100 text-emerald-700"
  } else if (status === "Rejected") {
    classes = "bg-red-100 text-red-700"
  }

  return (
    <div className={`text-xs rounded-md px-2 py-1 ${classes}`}>{status}</div>
  )
}


