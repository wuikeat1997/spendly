import { ImageResponse } from "next/og";

export const size = {
  width: 180,
  height: 180,
};

export const contentType = "image/png";

export default function AppleIcon() {
  return new ImageResponse(
    (
      <svg width="180" height="180" viewBox="0 0 512 512" fill="none">
        <rect width="512" height="512" rx="112" fill="#F3EFE5" />
        <path
          d="M256 48C214 78 168 100 116 114C98 119 86 135 86 154V282C86 364 139 432 256 480C373 432 426 364 426 282V154C426 135 414 119 396 114C344 100 302 78 256 48Z"
          fill="#13261A"
        />
        <path
          d="M256 101C223 122 186 139 146 151C137 154 130 162 130 172V280C130 343 170 398 256 438C342 398 382 343 382 280V172C382 162 375 154 366 151C326 139 289 122 256 101Z"
          fill="#FFF8EA"
        />
        <path
          d="M256 134C229 152 199 166 166 175C158 177 152 185 152 194V252H382V194C382 185 376 177 368 175C335 166 305 152 278 134C271 129 263 129 256 134Z"
          fill="#86B393"
          fillOpacity="0.58"
        />
        <path
          d="M152 252H382V279C382 330 350 374 256 416C162 374 130 330 130 279V252H152Z"
          fill="#236948"
        />
        <path
          d="M69 225H207C247 225 278 236 291 258C308 285 286 312 240 331C209 344 194 359 194 378C194 399 214 416 250 435"
          stroke="#FFF8EA"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="50"
        />
        <path
          d="M69 225H207C247 225 278 236 291 258C308 285 286 312 240 331C209 344 194 359 194 378C194 399 214 416 250 435"
          stroke="#DA6A35"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="34"
        />
        <path
          d="M248 322L283 357L356 273"
          stroke="#FFF8EA"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="58"
        />
        <path
          d="M248 322L283 357L356 273"
          stroke="#DA6A35"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth="36"
        />
      </svg>
    ),
    size,
  );
}
